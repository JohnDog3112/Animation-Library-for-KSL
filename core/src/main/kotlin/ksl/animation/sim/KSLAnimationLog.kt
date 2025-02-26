package ksl.animation.sim

import ksl.animation.sim.events.MoveEvent
import ksl.animation.sim.events.QueueEvent
import ksl.animation.sim.events.ResourceEvent
import ksl.animation.sim.events.ObjectEvent
import ksl.animation.viewer.AnimationViewer

class KSLAnimationLog(logData: String, private val viewer: AnimationViewer) {
    val events: List<KSLLogEvent>
    val objects = mutableSetOf<String>()

    init {
        viewer.loadAnimationLog(this)

        events = logData.lines()
            .mapNotNull { parseLogLine(it) }
            .sortedBy { it.getTime() }

        // postprocess events for stuff like moving
        val lastEvent = mutableMapOf<String, KSLLogEvent>()
        objects.forEach { objectId ->
            events.forEach { event ->
                if (event.involvesObject(objectId)) {
                    val previous = lastEvent[objectId]
                    if (previous != null) {
                        if (previous is MoveEvent) {
                            previous.duration = event.getTime() - previous.getTime()
                        }
                    }

                    lastEvent[objectId] = event
                }
            }
        }
    }

    private fun parseLogLine(line: String): KSLLogEvent? {
        val parts = line.split(": ", limit = 2)
        if (parts.size < 2) return null

        val time = parts[0].toDoubleOrNull() ?: return null
        val tokens = parts[1].split(" ")

        return listOf(
            ObjectEvent(time, viewer, this),
            QueueEvent(time, viewer, this),
            ResourceEvent(time, viewer, this),
            MoveEvent(time, viewer, this)
        ).find { it.parse(tokens) }
    }
}
