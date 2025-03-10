package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.sim.KSLLogParsingException
import ksl.animation.common.renderables.KSLObject
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class ObjectEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_OBJECT = "OBJECT"
        const val KEYWORD_ADD = "ADD"
        const val KEYWORD_REMOVE = "REMOVE"
        const val KEYWORD_AT = "AT"
        const val KEYWORD_SIZED = "SIZED"
    }

    override var containsObjects = true

    lateinit var objectTypeId: String
    lateinit var objectId: String
    lateinit var action: String
    var position = Position(0.0, 0.0)
    var width = 1.0
    var height = 1.0

    // OBJECT ADD <OBJECT TYPE> AS <OBJECT ID> (AT <STATION ID>) (SIZED <WIDTH> <HEIGHT>)
    // OBJECT REMOVE <OBJECT ID>
    override fun parse(tokens: List<String>): Boolean {
        startParsing(tokens)
        if (parseKeyword() == KEYWORD_OBJECT) {
            action = parseKeyword()
            when (action) {
                KEYWORD_ADD -> {
                    objectTypeId = parseObjectTypeId()
                    next() // AS
                    objectId = parseObjectId()

                    while (hasNext()) {
                        when (parseKeyword()) {
                            KEYWORD_AT -> position = parseStation()
                            KEYWORD_SIZED -> {
                                width = parseNumber()
                                height = parseNumber()
                            }
                        }
                    }
                }
                KEYWORD_REMOVE -> objectId = parseObjectId()
                else -> throw KSLLogParsingException("Unknown action $action")
            }

            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) {
            if (action == KEYWORD_ADD) println("Object Event: Adding $objectId at $position with width of $width and height of $height")
            else if (action == KEYWORD_REMOVE) println("Object Event: Removing $objectId")
        }

        if (action == KEYWORD_ADD) {
            viewer.objects[objectId] = KSLObject(objectId, position, objectTypeId, width, height)
        } else if (action == KEYWORD_REMOVE) {
            val kslObject = viewer.objects[objectId]

            if (kslObject != null) {
                viewer.objects.remove(objectId)
            } else {
                throw RuntimeException("Object $objectId not found")
            }
        }
    }

    override fun involvesObject(objectId: String): Boolean {
        return this.objectId == objectId
    }
}
