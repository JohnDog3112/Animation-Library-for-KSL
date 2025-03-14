package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.KSLQueue
import ksl.animation.util.Position

class AddQueue(scene: AnimationScene, private val queueId: String) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLQueue(queueId, Position(-1.0, 0.0), Position(1.0, 0.0)))
    }

    override fun redo() {
        scene.addRenderable(KSLQueue(queueId, Position(-1.0, 0.0), Position(1.0, 0.0)))
    }

    override fun undo() {
        scene.queues.remove(queueId)
        scene.renderables.remove(queueId)
    }
}
