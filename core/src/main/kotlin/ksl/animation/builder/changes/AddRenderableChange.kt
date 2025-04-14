package ksl.animation.builder.changes

import com.badlogic.gdx.graphics.Color
import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.*
import ksl.animation.setup.ResourceState
import ksl.animation.util.Position

class AddObject(scene: AnimationScene, private val objectId: String) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLObject(objectId, Position(0.0, 0.0), "default_object_type"))
    }

    override fun redo() {
        scene.addRenderable(KSLObject(objectId, Position(0.0, 0.0), "default_object_type"))
    }

    override fun undo() {
        scene.queues.remove(objectId)
        scene.renderables.remove(objectId)
    }
}

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

class AddResource(scene: AnimationScene, private val resourceId: String) : AnimationChange(scene) {
    override fun apply() {
        val states = ArrayList<ResourceState>()
        states.add(ResourceState("default_state", "default_resource", true))
        scene.addRenderable(KSLResource(resourceId, Position(0.0, 0.0), states))
    }

    override fun redo() {
        val states = ArrayList<ResourceState>()
        states.add(ResourceState("default_state", "default_resource", true))
        scene.addRenderable(KSLResource(resourceId, Position(0.0, 0.0), states))
    }

    override fun undo() {
        scene.resources.remove(resourceId)
        scene.renderables.remove(resourceId)
    }
}

class AddStation(scene: AnimationScene, private val stationId: String) : AnimationChange(scene) {
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

class AddVariable(scene: AnimationScene, private val variableId: String) : AnimationChange(scene) {
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
