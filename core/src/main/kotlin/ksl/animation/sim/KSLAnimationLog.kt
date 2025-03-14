package ksl.animation.sim

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ksl.animation.sim.events.*
import ksl.animation.viewer.AnimationViewer

class KSLAnimationLog(logData: String, private val viewer: AnimationViewer, private val onLoadComplete: (() -> Unit)? = null) {
    var events = mutableListOf<KSLLogEvent>()
    val objects = mutableSetOf<String>()
    var startTime = 0.0
    var endTime = 0.0

    init {
        viewer.loadAnimationLog(this)

        // Start loading the log asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            loadLog(logData)
        }
    }

    private suspend fun loadLog(logData: String) {
        withContext(Dispatchers.IO) {
            events = logData.lines()
                .mapNotNull { parseLogLine(it) }
                .sortedBy { it.getTime() }
                .toMutableList()

            viewer.runInstantEvents()

            val lastEvent = mutableMapOf<String, KSLLogEvent>()

            objects.forEach { objectId ->
                events.forEach { event ->
                    if (event.involvesObject(objectId)) {
                        val previous = lastEvent[objectId]
                        if (previous is MoveEvent) {
                            previous.duration = event.getTime() - previous.getTime()
                        }
                        lastEvent[objectId] = event
                    }
                }
            }

            onLoadComplete?.invoke()
        }
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
            MoveEvent(time, viewer),
            AnimationEvent(time, viewer)
        ).find { it.parse(tokens) }
    }
}
