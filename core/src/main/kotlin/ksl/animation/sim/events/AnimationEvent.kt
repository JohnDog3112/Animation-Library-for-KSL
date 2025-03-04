package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.viewer.AnimationViewer

class AnimationEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_START = "START"
        const val KEYWORD_STOP = "STOP"
    }

    override fun parse(tokens: List<String>): Boolean {
        val keyword = tokens[0]

        if (keyword == KEYWORD_START) {
            viewer.animationLog.startTime = this.getTime()
            return true
        }

        if (keyword == KEYWORD_STOP) {
            viewer.animationLog.endTime = this.getTime()
            return true
        }

        return false
    }

    override fun execute() {}

    override fun involvesObject(objectId: String): Boolean {
        return true
    }
}
