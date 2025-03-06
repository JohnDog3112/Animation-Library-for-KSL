package ksl.animation.sim

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.kotcrab.vis.ui.VisUI
import ksl.animation.Main
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.events.MoveQuery
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer


class KSLObject(
    id: String,
    position: Position,
    val objectType: String,
    val width: Double = 1.0,
    val height: Double = 1.0
) : KSLRenderable(id, position) {
    constructor(kslObject: KSLAnimationObject.Object) : this(kslObject.id, kslObject.position, kslObject.objectType, kslObject.width, kslObject.height)

    private val renderedId = GlyphLayout(Main.defaultFont, id)

    fun render(x: Double, y: Double, width: Double, height: Double, viewer: AnimationViewer) {
        val objectType = viewer.objectTypes[this.objectType]
        if (objectType != null) {
            val texture = viewer.images[objectType.image]
            if (texture != null) {
                viewer.spriteBatch!!.begin()
                viewer.spriteBatch!!.draw(texture, (viewer.originX + x - width / 2).toFloat(), (viewer.originY + y - height / 2).toFloat(), width.toFloat(), height.toFloat())
                viewer.spriteBatch!!.end()
            } else {
                throw RuntimeException("Image ${objectType.image} is not found")
            }
        } else {
            throw RuntimeException("Base object ${this.objectType} is not found")
        }

        super.render(viewer)
    }

    override fun render(viewer: AnimationViewer) {
        val width = width * viewer.screenUnit
        val height = height * viewer.screenUnit
        val x = position.x * viewer.screenUnit
        val y = position.y * viewer.screenUnit

        this.render(x, y, width, height, viewer)
    }

    fun applyMovement(movement: MoveQuery): Boolean {
        val amount = movement.elapsedTime / movement.duration
        if (amount >= 1) {
            position = movement.endPosition
            return true
        } else {
            position = ((movement.endPosition - movement.startPosition) * MovementFunctions.applyFunction(movement.movementFunction, amount)) + movement.startPosition
            return false
        }
    }
}
