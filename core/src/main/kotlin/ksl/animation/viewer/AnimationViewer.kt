package ksl.animation.viewer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.Main
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
    val stations = mutableMapOf<String, KSLStation>()
    val movements = mutableListOf<MoveQuery>()

    var screenUnit = 0.0
    var originX = 0.0
    var originY = 0.0
    var showGridLines = true
    var showStations = false
    var showIds = false

    lateinit var animationLog: KSLAnimationLog
    var ticksPerSecond = 1.0
    var playing = false

    var spriteBatch: SpriteBatch? = null
    var shapeRenderer: ShapeRenderer? = null
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
            stations[it.id] = KSLStation(it)
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
        if (spriteBatch == null) spriteBatch = SpriteBatch()
        if (shapeRenderer == null) shapeRenderer = ShapeRenderer()

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
                } else {
                    throw RuntimeException("Object ${movement.objectId} not found")
                }
            }
        }

        spriteBatch!!.transformMatrix = Main.camera.view
        shapeRenderer!!.transformMatrix = Main.camera.view
        spriteBatch!!.projectionMatrix = Main.camera.projection
        shapeRenderer!!.projectionMatrix = Main.camera.projection

        screenUnit = Main.camera.viewportWidth / 10.0
        originX = Main.camera.viewportWidth / 2.0
        originY = Main.camera.viewportHeight / 2.0

        // render grid lines
        if (showGridLines) {
            shapeRenderer!!.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer!!.end()
        }

        objects.forEach { it.value.render(this) }
        queues.forEach { it.value.render(this) }
        resources.forEach { it.value.render(this) }
        if (showStations) stations.forEach { it.value.render(this) }
    }

    fun dispose() {
        if (spriteBatch != null) spriteBatch!!.dispose()
        if (shapeRenderer != null) shapeRenderer!!.dispose()
        images.forEach { it.value.dispose() }
    }
}
