package ksl.animation.sim.events

import ksl.animation.sim.*
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class MoveEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_MOVE = "MOVE"
        const val KEYWORD_AS = "AS"
    }

    override var containsObjects = true

    lateinit var objectId: String
    lateinit var startPosition: Position
    lateinit var endPosition: Position
    var movementFunction = MovementFunctions.LINEAR_FUNCTION
    var duration = 0.0

    // MOVE <OBJECT ID> FROM <STATION ID> TO <STATION ID> (AS <INSTANT|LINEAR|EASE_IN|EASE_OUT|EASE_IN_OUT>)
    override fun parse(tokens: List<String>): Boolean {
        startParsing(tokens)
        if (parseKeyword() == KEYWORD_MOVE) {
            objectId = parseObjectId()
            next() // FROM

            startPosition = parseStation()
            next() // TO

            endPosition = parseStation()

            if (hasNext() && parseKeyword() == KEYWORD_AS) {
                val function = parseKeyword()
                if (
                    function != MovementFunctions.LINEAR_FUNCTION &&
                    function != MovementFunctions.EASE_IN_FUNCTION &&
                    function != MovementFunctions.EASE_OUT_FUNCTION &&
                    function != MovementFunctions.EASE_IN_OUT_FUNCTION &&
                    function != MovementFunctions.INSTANT_FUNCTION
                ) {
                    throw KSLLogParsingException("Unknown easing function: $function")
                } else {
                    movementFunction = function
                }
            }

            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) println("Move Event: Moving $objectId from $startPosition to $endPosition with $movementFunction function")

        val kslObject = viewer.objects[objectId]
        if (kslObject != null) {
            viewer.movements.add(MoveQuery(objectId, startPosition, endPosition, 0.0, duration, movementFunction))
        } else {
            throw RuntimeException("Object $objectId not found")
        }
    }

    override fun involvesObject(objectId: String): Boolean {
        return this.objectId == objectId
    }
}

data class MoveQuery(
    val objectId: String,
    val startPosition: Position,
    val endPosition: Position,
    var elapsedTime: Double,
    val duration: Double,
    val movementFunction: String
)
