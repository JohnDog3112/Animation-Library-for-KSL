package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

class MoveQueueChange(
    scene: AnimationScene,
    private val queueId: String,
    private val oldStartPosition: Position,
    private val oldEndPosition: Position,
    private val newStartPosition: Position,
    private val newEndPosition: Position
) : AnimationChange(scene) {
    override fun undo() {
        scene.queues[queueId]?.startPosition = oldStartPosition
        scene.queues[queueId]?.endPosition = oldEndPosition
    }

    override fun redo() {
        scene.queues[queueId]?.startPosition = newStartPosition
        scene.queues[queueId]?.endPosition = newEndPosition
    }
}
