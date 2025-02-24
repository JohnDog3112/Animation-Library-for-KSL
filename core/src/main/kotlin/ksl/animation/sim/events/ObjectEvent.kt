package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.sim.KSLLogParsingException
import ksl.animation.sim.KSLObject
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class ObjectEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_ADD = "ADD"
        const val KEYWORD_REMOVE = "REMOVE"
        const val KEYWORD_AT = "AT"
        const val KEYWORD_SIZED = "SIZED"
    }

    lateinit var objectTypeId: String
    lateinit var objectId: String
    lateinit var action: String
    var position = Position(0.0, 0.0)
    var width = 1.0
    var height = 1.0

    // OBJECT ADD <OBJECT TYPE> AS <OBJECT ID> (AT <STATION ID>) (SIZED <WIDTH> <HEIGHT>)
    // OBJECT REMOVE <OBJECT ID>
    override fun parse(tokens: List<String>): Boolean {
        var currentToken = 0
        if (tokens[currentToken++] == "OBJECT") {
            action = tokens[currentToken++]
            when (action) {
                KEYWORD_ADD -> {
                    objectTypeId = tokens[currentToken++].trim('"')
                    currentToken++
                    objectId = tokens[currentToken++].trim('"')

                    while (currentToken < tokens.size) {
                        val keyword = tokens[currentToken++]
                        if (keyword == KEYWORD_AT) {
                            val stationId = tokens[currentToken++].trim('"')
                            val station = viewer.stations[stationId]
                            if (station != null) {
                                position = station.position
                            } else {
                                throw KSLLogParsingException("Station $stationId not found")
                            }
                        } else if (keyword == KEYWORD_SIZED) {
                            val width = tokens[currentToken++].toDoubleOrNull()
                            val height = tokens[currentToken++].toDoubleOrNull()

                            if (width != null && height != null) {
                                this.width = width
                                this.height = height
                            } else {
                                throw KSLLogParsingException("Width and height have invalid format")
                            }
                        }
                    }
                }
                KEYWORD_REMOVE -> {
                    objectId = tokens[currentToken].trim('"')
                }
                else -> throw KSLLogParsingException("Unknown action $action")
            }
            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) {
            if (action == KEYWORD_ADD) println("Object: Adding $objectId at $position with width of $width and height of $height")
            else if (action == KEYWORD_REMOVE) println("Object: Removing $objectId")
        }

        if (action == KEYWORD_ADD) {
            val objectType = viewer.objectTypes[objectTypeId]

            if (objectType != null) {
                viewer.objects[objectId] = KSLObject(objectId, objectTypeId, position, width, height)
            } else {
                throw RuntimeException("Object type $objectType not found")
            }
        } else if (action == KEYWORD_REMOVE) {
            val kslObject = viewer.objects[objectId]

            if (kslObject != null) {
                viewer.objects.remove(objectId)
            } else {
                throw RuntimeException("Object $objectId not found")
            }
        }
    }
}
