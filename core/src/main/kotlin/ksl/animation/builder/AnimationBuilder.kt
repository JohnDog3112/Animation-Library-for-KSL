package ksl.animation.builder

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import ksl.animation.builder.changes.AddQueue
import ksl.animation.builder.changes.AddVariable
import ksl.animation.builder.changes.AddResource
import ksl.animation.builder.changes.AddStation
import ksl.animation.common.AnimationScene
import ksl.animation.setup.ResourceStates
import ksl.animation.common.renderables.KSLRenderable
import ksl.animation.util.Position
import java.util.*

class AnimationBuilder(private val onObjectClick: (kslObject: KSLRenderable?) -> Unit) : AnimationScene() {
    private var count = 0
    var selectedObject: String = ""
    var snapToGrid = false

    init {
        try {
            val decodedBytes = Base64.getDecoder().decode(ResourceStates.DEFAULT_IMAGE)
            val pixmap = Pixmap(decodedBytes, 0, decodedBytes.size)
            images["DEFAULT"] = Pair(pixmap, Texture(pixmap))
//            pixmap.dispose()
        } catch (e: IllegalArgumentException) {
            println("Invalid Base64 string for image: DEFAULT")
        }
    }

    fun addObject(type: String) {
        when (type) {
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

    override fun render(delta: Float) {
        super.render(delta)

        renderables.forEach { it.value.render(this) }
    }

    fun onMouseDown(x: Int, y: Int, button: Int) {
        val mouse = Position(x.toDouble(), y.toDouble())
        var selectedAny = false

        renderables.forEach { (id, renderable) ->
            if (selectedObject.isEmpty()) {
                if (button == Input.Buttons.LEFT && renderable.pointInside(this@AnimationBuilder, mouse)) {
                    selectedObject = id
                    selectedAny = true
                    renderable.selected = true
                    onObjectClick.invoke(renderable)
                    return@forEach
                }
            }
        }

        if (selectedObject.isNotEmpty()) {
            val selected = renderables[selectedObject]
            if (selected!!.onMouseDown(this@AnimationBuilder, x, y, button, snapToGrid)) return
        }

        if (!selectedAny && button == Input.Buttons.LEFT) {
            renderables[selectedObject]?.selected = false
            selectedObject = ""
            onObjectClick.invoke(null)
        }
    }

    fun onMouseUp(x: Int, y: Int, button: Int) {
        renderables[selectedObject]?.onMouseUp(this@AnimationBuilder, x, y, button, snapToGrid)
    }

    fun onMouseMove(x: Int, y: Int) {
        val mouse = Position(x.toDouble(), y.toDouble())

        renderables.forEach { (id, renderable) ->
            renderable.highlighted = renderable.pointInside(this@AnimationBuilder, mouse)
        }

        renderables[selectedObject]?.onMouseMove(this@AnimationBuilder, x, y, snapToGrid)
    }
}
