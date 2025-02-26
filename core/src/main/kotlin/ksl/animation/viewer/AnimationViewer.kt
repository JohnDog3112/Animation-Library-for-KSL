package ksl.animation.viewer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.setup.KSLAnimation
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.*
import ksl.animation.sim.events.MoveQuery
import java.util.*
import kotlin.math.abs

class AnimationViewer {
    val images = mutableMapOf<String, Texture>()
    val objects = mutableMapOf<String, KSLObject>()
    val queues = mutableMapOf<String, KSLQueue>()
    val resources = mutableMapOf<String, KSLResource>()
    val objectTypes = mutableMapOf<String, KSLAnimationObject.ObjectType>()
    val stations = mutableMapOf<String, KSLAnimationObject.Station>()
    val movements = mutableListOf<MoveQuery>()

    var screenUnit = 0.0
    var originX = 0.0
    var originY = 0.0

    lateinit var animationLog: KSLAnimationLog
    var ticksPerSecond = 1.0
    var playing = false

    private var batch: SpriteBatch? = null
    private var renderer: ShapeRenderer? = null
    private var currentEvent = 0
    private var ticks = 0.0
    private var timer = 0.0

    fun loadAnimationSetup(animation: KSLAnimation) {
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

        // load base objects
        animation.objects.filterIsInstance<KSLAnimationObject.ObjectType>().forEach {
            objectTypes[it.id] = it
        }

        // load objects
        animation.objects.filterIsInstance<KSLAnimationObject.Object>().forEach {
            objects[it.id] = KSLObject(it)
        }

        // load stations
        animation.objects.filterIsInstance<KSLAnimationObject.Station>().forEach {
            stations[it.id] = it
        }

        // load queues
        animation.objects.filterIsInstance<KSLAnimationObject.Queue>().forEach {
            queues[it.id] = KSLQueue(it)
        }

        // load resources
        animation.objects.filterIsInstance<KSLAnimationObject.Resource>().forEach {
            resources[it.id] = KSLResource(it)
        }
    }

    fun loadAnimationLog(animationLog: KSLAnimationLog) {
        this.animationLog = animationLog
    }

    fun runInstantEvents() {
        // execute events that happen instantly
        this.animationLog.events.filter { abs(it.getTime() - this.animationLog.startTime) < 0.0001 }.forEach { event ->
            event.execute()
        }
    }

    fun render(delta: Float) {
        if (batch == null) batch = SpriteBatch()
        if (renderer == null) renderer = ShapeRenderer()

        screenUnit = Gdx.graphics.width / 10.0
        originX = Gdx.graphics.width / 2.0
        originY = Gdx.graphics.height / 2.0

        if (playing) timer += delta

        while (timer > 1 / ticksPerSecond) {
            timer -= 1 / ticksPerSecond
            ticks++

            while (currentEvent < animationLog.events.size) {
                val event = animationLog.events[currentEvent]
                if (event.getTime() > ticks) break

                event.execute()
                currentEvent++
            }
        }

        // update any moving objects
        if (playing) {
            val movementIterator = movements.iterator()
            while (movementIterator.hasNext()) {
                val movement = movementIterator.next()
                movement.elapsedTime += delta * ticksPerSecond
                if (movement.movementFunction == MovementFunctions.INSTANT_FUNCTION) movement.elapsedTime = movement.duration

                val kslObject = objects[movement.objectId]
                if (kslObject != null) {
                    if (kslObject.applyMovement(movement)) movementIterator.remove()
                    else break
                } else {
                    throw RuntimeException("Object ${movement.objectId} not found")
                }
            }
        }

        objects.forEach { it.value.render(batch!!, this) }
        queues.forEach { it.value.render(batch!!, renderer!!, this) }
        resources.forEach { it.value.render(batch!!, this) }

        // debug render stations
        stations.forEach {
            val x = it.value.position.x * screenUnit + originX
            val y = it.value.position.y * screenUnit + originY

            renderer!!.begin(ShapeRenderer.ShapeType.Filled)
            renderer!!.color = Color.RED
            renderer!!.rect((x - 10).toFloat(), (y - 10).toFloat(), 20f, 20f)
            renderer!!.end()
        }
    }

    fun dispose() {
        if (batch != null) batch!!.dispose()
        if (renderer != null) renderer!!.dispose()
        images.forEach { it.value.dispose() }
    }
}
