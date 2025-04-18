package ksl.animation.builder

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ksl.animation.builder.changes.*
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.*
import ksl.animation.setup.KSLAnimation
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position
import java.util.*

class AnimationBuilder(private val onObjectClick: (kslObject: KSLRenderable?) -> Unit) : AnimationScene() {
    companion object {
        private const val DEFAULT_RESOURCE_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAADLklEQVRoQ92aOWgVQRyH9yloo4JYeBE0JJGAIAgqWglRsBEU8ewSDIKFBBu1EIxaxUaDIoKC2nkh2hlQwUoxgpVXkhcL70LEo1DB4/s/ZsJm3x6zm/fyZmbgx+Rl5/h9OzO7szNTCmoU/gXBCopaiZag+UoLVCy1fEDvVfyWuIwGS0HwpBYWKKd4wHwTufehbjS7YElfyHcencHMm4JlBIVAAFhOhQfRVjS1aOWRfH/4fR0dx9TzvGXmAgFgHhX0o+15K8qZ/grp92Puo2k+IxAApqgudIx4lmnhE0z3lfyH0VlM/s0qKxMEiGkUcgL1ZBVWp+unpBtj9Hda+akgQMwk8020vk4mTYsdkO6M2W9JGRJBgFhEpjuo3bS2OqeTB8BGDL+OqycWBIgZJL6P5L1gU3iMmXWY/hE1lQQiT40dNhGEvFzF9M5MEFqjk0QXLYXQtrqAuRT2OK5FgJApxSskXcvmIF2rHfPvtMkoyC0ubLKZIOTtNuY3V4HQGqv550NHILTNNcA8kh9jLQKIS62hQcZapQICRCvRUBjMkZbBemWsDGmQXv5xxBHzUZtHgejVIKNcbXYUZBSIlhJtsxiA2Ne+Q2DNAtKJYdtfgFn3tEtAZJrcqCl6lkHT6/0CIjPcDaY5LE03ICAjmGux1KCprbKAyHfxXNMclqb7JCA/MTfdUoOmtn55BeJN1/JmsHvz+PXmhejNFMWPSaM8qB1/u5eZxrd692Eln7rDpq9Ry9K10Rojfi0+qHHix3KQgnFpSSh+gU6BLCR+iWxfMv2Mx2WMC9klroSq1XhHvuHTF7E1HTA2bytc5u7LbGRcSNvouUfKVZY9agfx02G80aPGyxziB2ipJTDP8LEWCBkfVSFxD1HByFb0NdToVZa7eNiC2e9JNzUVJDRm+vj7QINapg+Th7LqNgJRrbOL+ByarAMDshW9G4M3siDkujGIgpEjHCdR1WakSWU50kh37sFcbY9wRA2oQzXS1bahWh6qkbsvXelpDuhK0lwtEgMkhwr2oj1oIsecLpD/NGYm95hT3N2ilfTBszau6wNnSQfP5BDaCyQHz3Lf/bj6/wNIW7ItAq1QegAAAABJRU5ErkJggg=="
        private const val DEFAULT_OBJECT_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAk0lEQVRoQ+2S0QkAIBCFrv2HrmYQhAKDPp+QtmZm3/v9WT3ksYYVeSzIVKQikoG+liQWYyuC1UnDikhiMbYiWJ00rIgkFmMrgtVJw4pIYjG2IlidNKyIJBZjK4LVScOKSGIxtiJYnTSsiCQWYyuC1UnDikhiMbYiWJ00rIgkFmMrgtVJw4pIYjG2IlidNKyIJBZjD62iMgGPECk2AAAAAElFTkSuQmCC"
    }

    private var count = 0
    var selectedObject: String = ""
    var snapToGrid = false

    init {
        this.loadDefaultImage()
    }

    private fun loadDefaultImage() {
        try {
            val defaultResourceBytes = Base64.getDecoder().decode(DEFAULT_RESOURCE_IMAGE)
            val defaultResourcePixmap = Pixmap(defaultResourceBytes, 0, defaultResourceBytes.size)
            images["default_resource"] = Pair(defaultResourcePixmap, Texture(defaultResourcePixmap))

            val defaultObjectBytes = Base64.getDecoder().decode(DEFAULT_OBJECT_IMAGE)
            val defaultObjectPixmap = Pixmap(defaultObjectBytes, 0, defaultObjectBytes.size)
            images["default_object_type"] = Pair(defaultObjectPixmap, Texture(defaultObjectPixmap))

            objectTypes["default_object_type"] = KSLAnimationObject.ObjectType("default_object_type", "default_object_type")
        } catch (e: IllegalArgumentException) {
            println("Invalid Base64 string for default images")
        }
    }

    fun loadAnimationSetup(animation: KSLAnimation) {
        if (animation.builderSetup) {
            animation.objects.filterIsInstance<KSLAnimationObject.Image>().forEach {
                try {
                    val decodedBytes = Base64.getDecoder().decode(it.data)
                    val pixmap = Pixmap(decodedBytes, 0, decodedBytes.size)
                    images[it.id] = Pair(pixmap, Texture(pixmap))
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
        } else {
            var position = 0.0
            // load base objects
            animation.objects.filterIsInstance<KSLAnimationObject.ObjectType>().forEach {
                objectTypes[it.id] = it
            }

            // load objects
            animation.objects.filterIsInstance<KSLAnimationObject.Object>().forEach {
                addRenderable(KSLObject(it.id, Position(0.0, position), "default_object_type"))
                position++
            }

            // load stations
            animation.objects.filterIsInstance<KSLAnimationObject.Station>().forEach {
                addRenderable(KSLStation(it.id, Position(0.0, position)))
                position++
            }

            // load queues
            animation.objects.filterIsInstance<KSLAnimationObject.Queue>().forEach {
                addRenderable(KSLQueue(it.id, Position(-1.0, position), Position(1.0, position)))
                position++
            }

            // load resources
            animation.objects.filterIsInstance<KSLAnimationObject.Resource>().forEach {
                val states = ArrayList<ResourceState>()
                states.add(ResourceState("default_state", "default_resource", true))
                addRenderable(KSLResource(it.id, Position(0.0, 0.0), states))
                position++
            }

            animation.objects.filterIsInstance<KSLAnimationObject.Variable>().forEach {
                addRenderable(KSLVariable(it.id, Position(-1.0, position), 2.0, 1.0, "Variable", 2.0, 2, Color.BLACK))
                position++
            }
        }
    }

    override fun resetScene() {
        super.resetScene()
        this.loadDefaultImage()
        this.selectedObject = ""
        this.snapToGrid = false
        onObjectClick.invoke(null)
    }

    fun addObject(type: String) {
        when (type) {
            "object" -> {
                val id = "object_$count"
                count++
                applyChange(AddObject(this, id))
            }
            "queue" -> {
                val id = "queue_$count"
                count++
                applyChange(AddQueue(this, id))
            }
            "variable" -> {
                val id = "variable_$count"
                count++
                applyChange(AddVariable(this, id))
            }
            "resource" -> {
                val id = "resource_$count"
                count++
                applyChange(AddResource(this, id))
            }
            "station" -> {
                val id = "station_$count"
                count++
                applyChange(AddStation(this, id))
            }
        }
    }

    fun removeObject(kslObject: KSLRenderable) {
        if (kslObject.id == selectedObject) selectedObject = ""

        when (kslObject) {
            is KSLObject -> applyChange(RemoveObject(this, kslObject))
            is KSLQueue -> applyChange(RemoveQueue(this, kslObject))
            is KSLResource -> applyChange(RemoveResource(this, kslObject))
            is KSLStation -> applyChange(RemoveStation(this, kslObject))
            is KSLVariable -> applyChange(RemoveVariable(this, kslObject))
        }
    }

    fun copyObject(kslObject: KSLRenderable) {
        if (kslObject.id == selectedObject) selectedObject = ""

        when (kslObject) {
            is KSLObject -> applyChange(CopyObject(this, kslObject))
            is KSLQueue -> applyChange(CopyQueue(this, kslObject))
            is KSLResource -> applyChange(CopyResource(this, kslObject))
            is KSLStation -> applyChange(CopyStation(this, kslObject))
            is KSLVariable -> applyChange(CopyVariable(this, kslObject))
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

        renderables.forEach { it.value.render(this) }
    }

    fun onMouseDown(x: Int, y: Int, button: Int) {
        val mouse = Position(x.toDouble(), y.toDouble())
        var selectedAny = false

        renderables.forEach { (id, renderable) ->
            renderable.selected = false
            if (button == Input.Buttons.LEFT && renderable.pointInside(this@AnimationBuilder, mouse)) {
                selectedObject = id
                selectedAny = true
                renderable.selected = true
                onObjectClick.invoke(renderable)
                return@forEach
            }
        }

        if (selectedObject.isNotEmpty()) {
            val selected = renderables[selectedObject]
            if (selected!!.onMouseDown(this, x, y, button, snapToGrid)) return
        }

        if (!selectedAny && button == Input.Buttons.LEFT) {
            renderables[selectedObject]?.selected = false
            renderables[selectedObject]?.closeEditor(this)
            selectedObject = ""
            onObjectClick.invoke(null)
        }
    }

    fun onMouseUp(x: Int, y: Int, button: Int) {
        renderables[selectedObject]?.onMouseUp(this, x, y, button, snapToGrid)
    }

    fun onMouseMove(x: Int, y: Int) {
        val mouse = Position(x.toDouble(), y.toDouble())

        renderables.forEach { (id, renderable) ->
            renderable.highlighted = renderable.pointInside(this@AnimationBuilder, mouse)
        }

        renderables[selectedObject]?.onMouseMove(this@AnimationBuilder, x, y, snapToGrid)
    }
}
