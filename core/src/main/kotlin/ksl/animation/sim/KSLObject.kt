package ksl.animation.sim

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class KSLObject(
    val id: String,
    val objectType: String,
    var position: Position = Position(0.0, 0.0),
    val width: Double = 1.0,
    val height: Double = 1.0
) {
    constructor(kslObject: KSLAnimationObject.Object) : this(kslObject.id, kslObject.objectType, kslObject.position, kslObject.width, kslObject.height)

    fun render(batch: SpriteBatch, viewer: AnimationViewer) {
        val width = width * viewer.screenUnit
        val height = height * viewer.screenUnit
        val x = position.x * viewer.screenUnit
        val y = position.y * viewer.screenUnit

        val objectType = viewer.objectTypes[this.objectType]
        if (objectType != null) {
            val texture = viewer.images[objectType.image]
            if (texture != null) {
                batch.begin()
                batch.draw(texture, (viewer.originX + x - width / 2).toFloat(), (viewer.originY + y - height / 2).toFloat(), width.toFloat(), height.toFloat())
                batch.end()
            } else {
                throw RuntimeException("Image ${objectType.image} is not found")
            }
        } else {
            throw RuntimeException("Base object ${this.objectType} is not found")
        }
    }
}
