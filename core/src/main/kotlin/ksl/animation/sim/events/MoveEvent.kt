package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.sim.KSLLogParsingException
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class MoveEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_MOVE = "MOVE"
        const val KEYWORD_AS = "AS"
        const val LINEAR_FUNCTION = "LINEAR"
        const val EASE_IN_FUNCTION = "EASE_IN"
        const val EASE_OUT_FUNCTION = "EASE_OUT"
        const val EASE_IN_OUT_FUNCTION = "EASE_IN_OUT"
    }

    lateinit var objectId: String
    lateinit var position: Position
    var movementFunction = LINEAR_FUNCTION
    var duration = 0.0

    // MOVE <OBJECT ID> TO <STATION ID> (AS <LINEAR|EASE_IN|EASE_OUT|EASE_IN_OUT>)
    override fun parse(tokens: List<String>): Boolean {
        var currentToken = 0
        if (tokens[currentToken++] == KEYWORD_MOVE) {
            objectId = tokens[currentToken++].trim('"')
            currentToken++
            val stationId = tokens[currentToken++].trim('"')
            val station = viewer.stations[stationId]
            if (station != null) {
                position = station.position
            } else {
                throw KSLLogParsingException("Station $stationId not found")
            }

            while (currentToken < tokens.size) {
                val keyword = tokens[currentToken++]
                if (keyword == KEYWORD_AS) {
                    val function = tokens[currentToken++].trim('"')
                    if (function != LINEAR_FUNCTION && function != EASE_IN_FUNCTION && function != EASE_OUT_FUNCTION && function != EASE_IN_OUT_FUNCTION) {
                        throw KSLLogParsingException("Unknown easing function: $function")
                    } else {
                        movementFunction = function
                    }
                }
            }
            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) println("Move Event: Moving $objectId to $position with $movementFunction")

        val kslObject = viewer.objects[objectId]
        if (kslObject != null) {
            viewer.movements.add(MoveQuery(objectId, kslObject.position, position, 0.0, duration, movementFunction))
        } else {
            throw RuntimeException("Object $objectId not found")
        }
    }
}

data class MoveQuery(
    val objectId: String,
    val startPosition: Position,
    val endPosition: Position,
    val elapsedTime: Double,
    val duration: Double,
    val movementFunction: String
)
