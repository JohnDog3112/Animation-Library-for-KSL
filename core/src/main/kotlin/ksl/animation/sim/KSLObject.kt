package ksl.animation.sim

import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class KSLObject(
    val id: String,
    val objectType: String,
    val position: Position = Position(0.0, 0.0),
    val width: Double = 1.0,
    val height: Double = 1.0
) {
    constructor(kslObject: KSLAnimationObject.Object) : this(kslObject.id, kslObject.objectType, kslObject.position, kslObject.width, kslObject.height)

    fun render(viewer: AnimationViewer, delta: Float) {

    }
}
