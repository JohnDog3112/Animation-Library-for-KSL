package ksl.animation.common.renderables

import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.MovementFunctions
import ksl.animation.sim.events.MoveQuery
import ksl.animation.util.Position

class KSLObject(
    id: String,
    position: Position,
    private val objectType: String,
    val width: Double = 1.0,
    val height: Double = 1.0
) : KSLRenderable(id, position) {
    constructor(kslObject: KSLAnimationObject.Object) : this(kslObject.id, kslObject.position, kslObject.objectType, kslObject.width, kslObject.height)

    var inQueue = false

    fun render(x: Double, y: Double, width: Double, height: Double, scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(Position(x, y))
        val translatedSize = Position(width, height) * scene.screenUnit

        val objectType = scene.objectTypes[this.objectType]
        if (objectType != null) {
            val texture = scene.images[objectType.image]
            if (texture != null) {
                scene.spriteBatch.begin()
                scene.spriteBatch.draw(texture, (translatedPosition.x - translatedSize.x / 2).toFloat(), (translatedPosition.y - translatedSize.y / 2).toFloat(), translatedSize.x.toFloat(), translatedSize.y.toFloat())
                scene.spriteBatch.end()
            } else {
                throw RuntimeException("Image ${objectType.image} is not found")
            }
        } else {
            throw RuntimeException("Base object ${this.objectType} is not found")
        }

        super.render(scene)
    }

    override fun render(scene: AnimationScene) {
        if (!inQueue) {
            this.render(position.x, position.y, width, height, scene)
        }
    }

    fun applyMovement(movement: MoveQuery): Boolean {
        val amount = movement.elapsedTime / movement.duration
        if (amount >= 1 || movement.movementFunction == MovementFunctions.INSTANT_FUNCTION) {
            position = movement.endPosition
            return true
        } else {
            position = ((movement.endPosition - movement.startPosition) * MovementFunctions.applyFunction(
                movement.movementFunction,
                amount
            )) + movement.startPosition
            return false
        }
    }
}
