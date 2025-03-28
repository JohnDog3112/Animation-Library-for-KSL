package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

class MoveAndResizeVariableChange(
    scene: AnimationScene,
    private val variableId: String,
    private val oldPosition: Position,
    private val oldSize: Position,
    private val newPosition: Position,
    private val newSize: Position
) : AnimationChange(scene) {
    override fun undo() {
        scene.variables[variableId]?.position = this.oldPosition
        scene.variables[variableId]?.width = this.oldSize.x
        scene.variables[variableId]?.height = this.oldSize.y
    }

    override fun redo() {
        scene.variables[variableId]?.position = this.newPosition
        scene.variables[variableId]?.width = this.newSize.x
        scene.variables[variableId]?.height = this.newSize.y
    }
}
