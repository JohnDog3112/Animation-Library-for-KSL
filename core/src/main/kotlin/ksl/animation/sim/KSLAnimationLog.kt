package ksl.animation.sim

import ksl.animation.sim.events.MoveEvent
import ksl.animation.sim.events.QueueEvent
import ksl.animation.sim.events.ResourceEvent
import ksl.animation.sim.events.ObjectEvent
import ksl.animation.viewer.AnimationViewer

class KSLAnimationLog(logData: String, private val viewer: AnimationViewer) {
    private val events: List<KSLLogEvent>

    init {
        events = logData.lines()
            .mapNotNull { parseLogLine(it) }
            .sortedBy { it.getTime() }

        // preprocess events for stuff like moving

    }

    fun getEvents(): List<KSLLogEvent> {
        return events
    }

    private fun parseLogLine(line: String): KSLLogEvent? {
        val parts = line.split(": ", limit = 2)
        if (parts.size < 2) return null

        val time = parts[0].toDoubleOrNull() ?: return null
        val tokens = parts[1].split(" ")

        return listOf(
            ObjectEvent(time, viewer),
            QueueEvent(time, viewer),
            ResourceEvent(time, viewer),
            MoveEvent(time, viewer)
        ).find { it.parse(tokens) }
    }
}
