package ksl.animation.builder.changes

import com.badlogic.gdx.graphics.Color
import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.KSLQueue
import ksl.animation.common.renderables.KSLResource
import ksl.animation.common.renderables.KSLStation
import ksl.animation.setup.ResourceState
import ksl.animation.common.renderables.KSLVariable
import ksl.animation.util.Position

class EditQueueSettings(
    scene: AnimationScene,
    private val queue: KSLQueue,
    private val previousId: String,
    private val id: String,
    private val previousScale: Double,
    private val scale: Double
) : AnimationChange(scene) {
    override fun apply() {
        queue.id = id
        queue.scale = scale
    }

    override fun redo() {
        queue.id = id
        queue.scale = scale
    }

    override fun undo() {
        queue.id = previousId
        queue.scale = previousScale
    }
}

class EditResourceSettings(scene: AnimationScene, private val resourceId: String) : AnimationChange(scene) {
    override fun apply() {
        val states = mutableListOf<ResourceState>()
        states.add(ResourceState("DEFAULT", "DEFAULT", true))
        scene.addRenderable(KSLResource(resourceId, Position(0.0, 0.0), states,0.5, 0.5))
    }

    override fun redo() {
        val defaultState = ResourceState("default", "default_image")
        scene.addRenderable(KSLResource(resourceId, Position(0.0, 0.0), listOf(defaultState)))
    }

    override fun undo() {
        scene.resources.remove(resourceId)
        scene.renderables.remove(resourceId)
    }
}

class EditStationSettings(scene: AnimationScene, private val stationId: String) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLStation(stationId, Position(0.0, 0.0)))
    }

    override fun redo() {
        scene.addRenderable(KSLStation(stationId, Position(0.0, 0.0)))
    }

    override fun undo() {
        scene.stations.remove(stationId)
        scene.renderables.remove(stationId)
    }
}

class EditVariableSettings(scene: AnimationScene, private val variableId: String) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLVariable(variableId, Position(-1.0, 0.0), 2.0, 1.0, "Variable", 2.0, 2, Color.BLACK))
    }

    override fun redo() {
        scene.addRenderable(KSLVariable(variableId, Position(-1.0, 0.0), 2.0, 1.0, "Variable", 2.0, 2, Color.BLACK))
    }

    override fun undo() {
        scene.variables.remove(variableId)
        scene.renderables.remove(variableId)
    }
}
