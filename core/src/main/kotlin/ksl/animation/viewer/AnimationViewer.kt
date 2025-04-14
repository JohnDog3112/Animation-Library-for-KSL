package ksl.animation.viewer

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.*
import ksl.animation.setup.KSLAnimation
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.*
import ksl.animation.util.KSLAnimationGlobals
import java.util.*
import kotlin.math.abs

class AnimationViewer : AnimationScene() {
    var showStations = false

    lateinit var animationLog: KSLAnimationLog
    var ticksPerSecond = 1.0
    var playing = false

    private var currentEvent = 0
    var ticks = 0.0
    private var timer = 0.0

    fun loadAnimationSetup(animation: KSLAnimation) {
        animation.objects.filterIsInstance<KSLAnimationObject.Image>().forEach {
            try {
                val decodedBytes = Base64.getDecoder().decode(it.data)
                val pixmap = Pixmap(decodedBytes, 0, decodedBytes.size)
                images[it.id] = Pair(pixmap, Texture(pixmap))
//                pixmap.dispose()
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
            addRenderable(KSLObject(it))
        }

        // load stations
        animation.objects.filterIsInstance<KSLAnimationObject.Station>().forEach {
            addRenderable(KSLStation(it))
        }

        // load queues
        animation.objects.filterIsInstance<KSLAnimationObject.Queue>().forEach {
            addRenderable(KSLQueue(it))
        }

        // load resources
        animation.objects.filterIsInstance<KSLAnimationObject.Resource>().forEach {
            addRenderable(KSLResource(it))
        }

        animation.objects.filterIsInstance<KSLAnimationObject.Variable>().forEach {
            addRenderable(KSLVariable(it))
        }
    }

    fun loadAnimationLog(animationLog: KSLAnimationLog) {
        this.animationLog = animationLog
    }

    fun runInstantEvents() {
        // execute events that happen instantly
        this.animationLog.events.filter { abs(it.getTime() - this.animationLog.startTime) < 0.0001 }.forEach { event ->
            event.execute()
            currentEvent++
        }
    }


    fun reset() {
        ticks = 0.0
        timer = 0.0
        playing = false
        currentEvent = 0

        // Clear all animation objects
        movements.clear()
        objects.clear()
        stations.clear()
        queues.clear()
        resources.clear()
    }



    override fun render(delta: Float) {
        if (playing) timer += delta

        while (timer > 1 / ticksPerSecond) {
            timer -= 1 / ticksPerSecond
            ticks++

            if (ticks > animationLog.endTime) playing = false

            while (currentEvent < animationLog.events.size) {
                val event = animationLog.events[currentEvent]
                if (event.getTime() > ticks) break

                try {
                    event.execute()
                } catch (e: RuntimeException) {
                    println(e.message)
                }
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
                    if (KSLAnimationGlobals.VERBOSE) println("Object ${movement.objectId} not found")
                    movementIterator.remove()
                }
            }
        }

        super.render(delta)

        objects.forEach { it.value.render(this) }
        queues.forEach { it.value.render(this) }
        resources.forEach { it.value.render(this) }
        variables.forEach { it.value.render(this) }
        if (showStations) stations.forEach { it.value.render(this) }
    }

    override fun dispose() {
        images.forEach {
            it.value.first.dispose()
            it.value.second.dispose()
        }
        super.dispose()
    }
}
