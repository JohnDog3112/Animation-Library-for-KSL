package ksl.animation.sim

import ksl.animation.viewer.AnimationViewer

abstract class KSLLogEvent(private val time: Double, protected val viewer: AnimationViewer, protected val animationLog: KSLAnimationLog) {
    open var containsObjects = false

    fun getTime(): Double {
        return time
    }

    abstract fun execute()
    abstract fun parse(tokens: List<String>): Boolean
    abstract fun involvesObject(objectId: String): Boolean
}

class KSLLogParsingException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
