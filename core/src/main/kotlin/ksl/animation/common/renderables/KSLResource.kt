package ksl.animation.common.renderables

import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position

class KSLResource(id: String, position: Position, states: List<ResourceState>, private val width: Double = 1.0, private val height: Double = 1.0) : KSLRenderable(id, position) {
    constructor(resourceObject: KSLAnimationObject.Resource) : this(resourceObject.id, resourceObject.position, resourceObject.states, resourceObject.width, resourceObject.height)

    private val resourceStates = ResourceStates(states)
    private var currentState = resourceStates.defaultState.name

    fun setState(state: String) {
        currentState = state
    }

    override fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = Position(width, height) * scene.screenUnit

        val texture = resourceStates.getImage(currentState, scene.images)
        scene.spriteBatch.begin()
        scene.spriteBatch.draw(texture, (translatedPosition.x - translatedSize.x / 2).toFloat(), (translatedPosition.y - translatedSize.y / 2).toFloat(), translatedSize.x.toFloat(), translatedSize.y.toFloat())
        scene.spriteBatch.end()

        super.render(scene)
    }
}
