package ksl.animation.common

open class AnimationChange(protected val scene: AnimationScene) {
    open fun apply() {}
    open fun undo() {}
    open fun redo() {}
}
