package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

class MoveResourceChange(
    scene: AnimationScene,
    private val resourceId: String,
    private val oldPosition: Position,
    private val newPosition: Position
) : AnimationChange(scene) {
    override fun undo() {
        scene.resources[resourceId]?.position = oldPosition
    }

    override fun redo() {
        scene.resources[resourceId]?.position = newPosition
    }
}
