package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

class MoveStationChange(scene: AnimationScene, private val stationId: String, private val oldPosition: Position, private val newPosition: Position, ) : AnimationChange(scene) {

    override fun undo() {
        scene.queues[stationId]?.startPosition = oldPosition
    }

    override fun redo() {
        scene.queues[stationId]?.startPosition = newPosition
    }
}
