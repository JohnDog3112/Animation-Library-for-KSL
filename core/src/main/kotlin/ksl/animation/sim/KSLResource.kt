package ksl.animation.sim

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class KSLResource(states: List<ResourceState>, private val position: Position = Position(0.0, 0.0), private val width: Double = 1.0, private val height: Double = 1.0) {
    constructor(resourceObject: KSLAnimationObject.Resource) : this(resourceObject.states, resourceObject.position, resourceObject.width, resourceObject.height)

    private val resourceStates = ResourceStates(states)
    private var currentState = "idle"

    fun setState(state: String) {
        currentState = state
    }

    fun render(batch: SpriteBatch, viewer: AnimationViewer) {
        val texture = resourceStates.getImage(currentState, viewer.images)
        batch.begin()
        batch.draw(texture, (viewer.originX + position.x * viewer.screenUnit).toFloat(), (viewer.originY + position.y * viewer.screenUnit).toFloat(), (width * viewer.screenUnit).toFloat(), (height * viewer.screenUnit).toFloat())
        batch.end()
    }
}
