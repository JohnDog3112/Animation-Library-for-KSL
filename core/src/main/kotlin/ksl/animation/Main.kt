package ksl.animation

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.async.KtxAsync
import ktx.assets.*
import ktx.scene2d.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ktx.graphics.use
import java.util.Base64
import kotlin.math.sqrt

@Serializable
data class KSLAnimation(
    val objects: List<KSLAnimationObject>
)

@Serializable
data class Position(val x: Float, val y: Float)

@Serializable
class ResourceStates(private val idle: String, private val active: String, private val busy: String) {
    fun getImage(state: String) : String {
        if (state == "active") return active
        if (state == "busy") return busy
        return idle
    }
}

@Serializable
sealed class KSLAnimationObject {
    @Serializable
    @SerialName("image")
    data class Image(val id: String, val data: String) : KSLAnimationObject()

    @Serializable
    @SerialName("queue")
    data class Queue(val id: String, val startPosition: Position, val endPosition: Position, val scale: Float) : KSLAnimationObject()

    @Serializable
    @SerialName("resource")
    data class Resource(val id: String, val position: Position, val states: ResourceStates, val scale: Float) : KSLAnimationObject()

    @Serializable
    @SerialName("base_entity")
    data class BaseEntity(val id: String, val image: String) : KSLAnimationObject()

    @Serializable
    @SerialName("entity")
    data class Entity(val id: String, val baseEntity: String, val position: Position, val scale: Float, val width: Float, val height: Float) : KSLAnimationObject()
}

@Serializable
sealed class LogEvent {
    @Serializable
    @SerialName("spawn")
    data class Spawn(val time: Double, val entityType: String, val entityId: String) : LogEvent()

    @Serializable
    @SerialName("queue")
    data class QueueEvent(val time: Double, val queueId: String, val action: String, val entityId: String) : LogEvent()

    @Serializable
    @SerialName("resource")
    data class ResourceEvent(val time: Double, val resourceId: String, val action: String, val state: String) : LogEvent()
}

class KSLAnimationLog(logData: String) {
    val events: List<LogEvent>

    init {
        events = logData.lines()
            .mapNotNull { parseLogLine(it) }
            .sortedBy { event ->
                when (event) {
                    is LogEvent.Spawn -> event.time
                    is LogEvent.QueueEvent -> event.time
                    is LogEvent.ResourceEvent -> event.time
                }
            }
    }

    private fun parseLogLine(line: String): LogEvent? {
        val parts = line.split(": ", limit = 2)
        if (parts.size < 2) return null

        val time = parts[0].toDoubleOrNull() ?: return null
        val eventParts = parts[1].split(" ")

        return when (eventParts[0]) {
            "SPAWN" -> LogEvent.Spawn(time, eventParts[1].trim('"'), eventParts[3].trim('"'))
            "QUEUE" -> LogEvent.QueueEvent(time, eventParts[1].trim('"'), eventParts[2], eventParts[3].trim('"'))
            "RESOURCE" -> LogEvent.ResourceEvent(time, eventParts[1].trim('"'), eventParts[3], eventParts[4].trim('"'))
            else -> null
        }
    }
}

class Main : KtxGame<KtxScreen>() {
    private val manager = AssetManager()
    private var loaded = false

    override fun create() {
        KtxAsync.initiate()

        manager.setLoader(TextAssetLoader())
        manager.load<String>("setup.json")
        manager.load<String>("sim.log")

        Gdx.graphics.setWindowedMode(1080, 720)

        addScreen(LoadingScreen())
        setScreen<LoadingScreen>()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun render() {
        if (manager.update() && !loaded) {
            val json = Json {
                namingStrategy = JsonNamingStrategy.SnakeCase
                serializersModule = SerializersModule {
                    polymorphic(KSLAnimationObject::class) {
                        subclass(KSLAnimationObject.Image::class, KSLAnimationObject.Image.serializer())
                        subclass(KSLAnimationObject.Queue::class, KSLAnimationObject.Queue.serializer())
                        subclass(KSLAnimationObject.Resource::class, KSLAnimationObject.Resource.serializer())
                        subclass(KSLAnimationObject.BaseEntity::class, KSLAnimationObject.BaseEntity.serializer())
                        subclass(KSLAnimationObject.Entity::class, KSLAnimationObject.Entity.serializer())
                    }
                }
            }

            loaded = true
            addScreen(DemoScreen(json.decodeFromString<KSLAnimation>(manager.get("setup.json")), KSLAnimationLog(manager.get("sim.log"))))
            setScreen<DemoScreen>()
        }

        super.render()
    }
}

class LoadingScreen : KtxScreen {
    private var stage: Stage = Stage()

    init {
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))

        stage.actors {
            table {
                setFillParent(true)
                background("white")
                label("Hello world!") {
                    color = Color.RED
                }
            }
        }

        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}

class KSLQueue(private val startPosition: Position, private val endPosition: Position, private val scale: Float) {
    private val entities = mutableMapOf<String, KSLAnimationObject.Entity>()

    fun addEntity(entity: KSLAnimationObject.Entity) {
        entities[entity.id] = entity
    }

    fun removeEntity(entityId: String) {
        entities.remove(entityId)
    }

    fun drawQueue(batch: SpriteBatch, images: Map<String, Texture>, baseEntities: Map<String, KSLAnimationObject.BaseEntity>, shapeRenderer: ShapeRenderer) {
        val screenUnit = Gdx.graphics.width.toFloat() / 10
        val originX = Gdx.graphics.width.toFloat() / 2
        val originY = Gdx.graphics.height.toFloat() / 2

        // draw queue line
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.BLACK
        Gdx.gl.glLineWidth(scale * 5f)
        val startX = startPosition.x * screenUnit
        val startY = startPosition.y * screenUnit
        val endX = endPosition.x * screenUnit
        val endY = endPosition.y * screenUnit
        shapeRenderer.line(originX + startX, originY + startY, originX + endX, originY + endY)

        // create line perpendicular to end point
        val dx = endX - startX
        val dy = endY - startY
        val length = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val perpX = -dy / length * (10 * scale)
        val perpY = dx / length * (10 * scale)

        shapeRenderer.line(originX + endX - perpX, originY + endY - perpY, originX + endX + perpX, originY + endY + perpY)
        shapeRenderer.end()

        val normalizeX = dx / length
        val normalizeY = dy / length

        entities.entries.forEachIndexed { index, entity ->
            val width = entity.value.width * screenUnit
            val height = entity.value.height * screenUnit
            val x = endX - (width * normalizeX * index)
            val y = endY - (height * normalizeY * index)

            batch.begin()
            batch.draw(images[baseEntities[entity.value.baseEntity]?.image], originX + x - width / 2, originY + y - height / 2, width, height)
            batch.end()
        }
    }
}

class KSLResource(val position: Position, val states: ResourceStates, val scale: Float) {
    private var currentState = "idle"

    fun setState(state: String) {
        currentState = state
    }

    fun drawResource(batch: SpriteBatch, images: Map<String, Texture>) {
        val screenUnit = Gdx.graphics.width.toFloat() / 10
        val originX = Gdx.graphics.width.toFloat() / 2
        val originY = Gdx.graphics.height.toFloat() / 2

        val texture = states.getImage(currentState)
        images[texture]?.let {
            batch.begin()
            batch.draw(it, originX + position.x * screenUnit, originY + position.y * screenUnit, scale * screenUnit, scale * screenUnit)
            batch.end()
        }
    }
}

class DemoScreen(animation: KSLAnimation, private val animationLog: KSLAnimationLog) : KtxScreen {
    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val images = mutableMapOf<String, Texture>()
    private val baseEntities = mutableMapOf<String, KSLAnimationObject.BaseEntity>()
    private val entities = mutableMapOf<String, KSLAnimationObject.Entity>()
    private val queues = mutableMapOf<String, KSLQueue>()
    private val resources = mutableMapOf<String, KSLResource>()
    private val sprites = mutableListOf<Sprite>()
    private var currentEvent = 0
    private var ticks = 0
    private var ticksPerSecond = 5
    private var timer = 0f

    init {
        // load images
        animation.objects.filterIsInstance<KSLAnimationObject.Image>().forEach {
            try {
                val decodedBytes = Base64.getDecoder().decode(it.data)
                val pixmap = Pixmap(decodedBytes, 0, decodedBytes.size)
                images[it.id] = Texture(pixmap)
                pixmap.dispose()
            } catch (e: IllegalArgumentException) {
                println("Invalid Base64 string for image: ${it.id}")
            }
        }

        // load base entities
        animation.objects.filterIsInstance<KSLAnimationObject.BaseEntity>().forEach {
            baseEntities[it.id] = it
        }

        // add queue elements
        animation.objects.filterIsInstance<KSLAnimationObject.Queue>().forEach {
            queues[it.id] = KSLQueue(it.startPosition, it.endPosition, it.scale)
        }

        // add resource elements
        animation.objects.filterIsInstance<KSLAnimationObject.Resource>().forEach {
            resources[it.id] = KSLResource(it.position, it.states, it.scale)
        }
    }

    override fun render(delta: Float) {
        // Update animation timer
        timer += delta

        // Process ticks
        while (timer > 1f / ticksPerSecond) {
            timer -= 1f / ticksPerSecond
            ticks++

            // Ensure we don't go out of bounds
            while (currentEvent < animationLog.events.size) {
                val event = animationLog.events[currentEvent]
                val eventTime = when (event) {
                    is LogEvent.Spawn -> event.time
                    is LogEvent.QueueEvent -> event.time
                    is LogEvent.ResourceEvent -> event.time
                }

                if (eventTime > ticks) break // Stop processing if the event is in the future

                // Execute the event
                processEvent(event)

                // Move to the next event
                currentEvent++
            }
        }

        // Clear the screen
        clearScreen(red = 1f, green = 1f, blue = 1f)

        // Render sprites
        batch.use {
            sprites.forEach { it.draw(batch) }
        }

        // Draw elements
        queues.forEach { it.value.drawQueue(batch, images, baseEntities, shapeRenderer) }
        resources.forEach { it.value.drawResource(batch, images) }
    }

    private fun processEvent(event: LogEvent) {
        when (event) {
            is LogEvent.Spawn -> {
                println("Spawning entity: ${event.entityId}")
                baseEntities[event.entityType]?.let { base ->
                    val entity = KSLAnimationObject.Entity(
                        id = event.entityId,
                        baseEntity = event.entityType,
                        position = Position(0f, 0f),
                        scale = 1f,
                        width = 1f,
                        height = 1f
                    )
                    entities[event.entityId] = entity
                    images[base.image]?.let { texture ->
                        sprites.add(Sprite(texture))
                    }
                }
            }

            is LogEvent.QueueEvent -> {
                println("Queue Event: ${event.action} on ${event.queueId}")
                queues[event.queueId]?.let { queue ->
                    when (event.action) {
                        "JOIN" -> {
                            entities[event.entityId]?.let { entity ->
                                queue.addEntity(entity)
                            }
                        }
                        "LEAVE" -> {
                            queue.removeEntity(event.entityId)
                        }
                        else -> {}
                    }
                }
            }

            is LogEvent.ResourceEvent -> {
                println("Resource Event: ${event.state} on ${event.resourceId}")
                resources[event.resourceId]?.setState(event.state)
            }
        }
    }


    override fun dispose() {
        batch.dispose()
        sprites.forEach { it.texture.dispose() }
    }
}
