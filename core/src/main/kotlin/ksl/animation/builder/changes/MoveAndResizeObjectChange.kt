package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

class MoveAndResizeObjectChange(
    scene: AnimationScene,
    private val id: String,
    private val oldPosition: Position,
    private val oldSize: Position,
    private val newPosition: Position,
    private val newSize: Position
) : AnimationChange(scene) {
    override fun undo() {
        if (scene.variables[id] != null) {
            scene.variables[id]?.position = this.oldPosition
            scene.variables[id]?.width = this.oldSize.x
            scene.variables[id]?.height = this.oldSize.y
        } else if (scene.resources[id] != null) {
            scene.resources[id]?.position = this.oldPosition
            scene.resources[id]?.width = this.oldSize.x
            scene.resources[id]?.height = this.oldSize.y
        }
    }

    override fun redo() {
        if (scene.variables[id] != null) {
            scene.variables[id]?.position = this.newPosition
            scene.variables[id]?.width = this.newSize.x
            scene.variables[id]?.height = this.newSize.y
        } else if (scene.resources[id] != null) {
            scene.resources[id]?.position = this.newPosition
            scene.resources[id]?.width = this.newSize.x
            scene.resources[id]?.height = this.newSize.y
        }
    }
}
