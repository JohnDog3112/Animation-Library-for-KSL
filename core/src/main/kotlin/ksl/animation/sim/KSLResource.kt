package ksl.animation.sim

import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class KSLResource(id: String, position: Position, states: List<ResourceState>, private val width: Double = 1.0, private val height: Double = 1.0) : KSLRenderable(id, position) {
    constructor(resourceObject: KSLAnimationObject.Resource) : this(resourceObject.id, resourceObject.position, resourceObject.states, resourceObject.width, resourceObject.height)

    private val resourceStates = ResourceStates(states)
    private var currentState = resourceStates.defaultState.name

    fun setState(state: String) {
        currentState = state
    }

    override fun render(viewer: AnimationViewer) {
        val texture = resourceStates.getImage(currentState, viewer.images)
        viewer.spriteBatch!!.begin()
        viewer.spriteBatch!!.draw(texture, (viewer.originX + position.x * viewer.screenUnit).toFloat(), (viewer.originY + position.y * viewer.screenUnit).toFloat(), (width * viewer.screenUnit).toFloat(), (height * viewer.screenUnit).toFloat())
        viewer.spriteBatch!!.end()

        super.render(viewer)
    }
}
