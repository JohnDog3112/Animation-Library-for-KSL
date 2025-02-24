package ksl.animation.sim

import ksl.animation.viewer.AnimationViewer

abstract class KSLLogEvent(private val time: Double, protected val viewer: AnimationViewer) {
    fun getTime(): Double {
        return time
    }

    abstract fun execute()
    abstract fun parse(tokens: List<String>): Boolean
}

class KSLLogParsingException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
