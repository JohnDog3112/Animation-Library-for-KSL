package ksl.animation.builder

import com.badlogic.gdx.Input
import ksl.animation.builder.changes.AddQueue
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.KSLRenderable
import ksl.animation.util.Position

class AnimationBuilder(private val onObjectClick: (kslObject: KSLRenderable?) -> Unit) : AnimationScene() {
    private var count = 0
    var selectedObject: String = ""
    var snapToGrid = false

    fun addObject(type: String) {
        when (type) {
            "queue" -> {
                val id = "queue_$count"
                count++
                applyChange(AddQueue(this, id))
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
