package ksl.animation.builder.changes

import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.KSLQueue
import ksl.animation.common.renderables.KSLResource
import ksl.animation.common.renderables.KSLStation
import ksl.animation.common.renderables.KSLVariable
import ksl.animation.util.Position

class CopyQueue(scene: AnimationScene, private val queue: KSLQueue) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLQueue(queue.id + "_copy", queue.startPosition + Position(0.0, 1.0), queue.endPosition + Position(0.0, 1.0), queue.scale))
    }

    override fun redo() {
        scene.addRenderable(KSLQueue(queue.id + "_copy", queue.startPosition + Position(0.0, 1.0), queue.endPosition + Position(0.0, 1.0), queue.scale))
    }

    override fun undo() {
        scene.queues.remove(queue.id + "_copy")
        scene.renderables.remove(queue.id + "_copy")
    }
}

class CopyResource(scene: AnimationScene, private val resource: KSLResource) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLResource(resource.id + "_copy", resource.position + Position(0.0, 1.0), resource.states, resource.width, resource.height))
    }

    override fun redo() {
        scene.addRenderable(KSLResource(resource.id + "_copy", resource.position + Position(0.0, 1.0), resource.states, resource.width, resource.height))
    }

    override fun undo() {
        scene.resources.remove(resource.id + "_copy")
        scene.renderables.remove(resource.id + "_copy")
    }
}

class CopyStation(scene: AnimationScene, private val station: KSLStation) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLStation(station.id + "_copy", station.position))
    }

    override fun redo() {
        scene.addRenderable(KSLStation(station.id + "_copy", station.position))
    }

    override fun undo() {
        scene.stations.remove(station.id + "_copy")
        scene.renderables.remove(station.id + "_copy")
    }
}

class CopyVariable(scene: AnimationScene, private val variable: KSLVariable) : AnimationChange(scene) {
    override fun apply() {
        scene.addRenderable(KSLVariable(variable.id + "_copy", variable.position, variable.width, variable.height, variable.defaultValue, variable.maxTextScale, variable.precision, variable.textColor))
    }

    override fun redo() {
        scene.addRenderable(KSLVariable(variable.id + "_copy", variable.position, variable.width, variable.height, variable.defaultValue, variable.maxTextScale, variable.precision, variable.textColor))
    }

    override fun undo() {
        scene.variables.remove(variable.id + "_copy")
        scene.renderables.remove(variable.id + "_copy")
    }
}
