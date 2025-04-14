package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.*

class RemoveObject(scene: AnimationScene, private val kslObject: KSLObject) : AnimationChange(scene) {
    override fun apply() {
        scene.queues.remove(kslObject.id)
        scene.renderables.remove(kslObject.id)
    }

    override fun redo() {
        scene.addRenderable(kslObject)
    }

    override fun undo() {
        scene.addRenderable(kslObject)
    }
}

class RemoveQueue(scene: AnimationScene, private val queue: KSLQueue) : AnimationChange(scene) {
    override fun apply() {
        scene.queues.remove(queue.id)
        scene.renderables.remove(queue.id)
    }

    override fun redo() {
        scene.addRenderable(queue)
    }

    override fun undo() {
        scene.addRenderable(queue)
    }
}

class RemoveResource(scene: AnimationScene, private val resource: KSLResource) : AnimationChange(scene) {
    override fun apply() {
        scene.resources.remove(resource.id)
        scene.renderables.remove(resource.id)
    }

    override fun redo() {
        scene.addRenderable(resource)
    }

    override fun undo() {
        scene.addRenderable(resource)
    }
}

class RemoveStation(scene: AnimationScene, private val station: KSLStation) : AnimationChange(scene) {
    override fun apply() {
        scene.stations.remove(station.id)
        scene.renderables.remove(station.id)
    }

    override fun redo() {
        scene.addRenderable(station)
    }

    override fun undo() {
        scene.addRenderable(station)
    }
}

class RemoveVariable(scene: AnimationScene, private val variable: KSLVariable) : AnimationChange(scene) {
    override fun apply() {
        scene.variables.remove(variable.id)
        scene.renderables.remove(variable.id)
    }

    override fun redo() {
        scene.addRenderable(variable)
    }

    override fun undo() {
        scene.addRenderable(variable)
    }
}
