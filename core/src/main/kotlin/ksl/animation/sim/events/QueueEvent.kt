package ksl.animation.sim.events

import ksl.animation.sim.KSLLogEvent
import ksl.animation.sim.KSLLogParsingException
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.viewer.AnimationViewer

class QueueEvent(time: Double, viewer: AnimationViewer) : KSLLogEvent(time, viewer) {
    companion object {
        const val KEYWORD_QUEUE = "QUEUE"
        const val KEYWORD_JOIN = "JOIN"
        const val KEYWORD_LEAVE = "LEAVE"
    }

    lateinit var queueId: String
    lateinit var objectId: String
    lateinit var action: String

    // QUEUE <QUEUE ID> JOIN <OBJECT ID>
    // QUEUE <QUEUE ID> LEAVE <OBJECT ID>
    override fun parse(tokens: List<String>): Boolean {
        var currentToken = 0
        if (tokens[currentToken++] == KEYWORD_QUEUE) {
            queueId = tokens[currentToken++].trim('"')
            action = tokens[currentToken++]
            objectId = tokens[currentToken].trim('"')

            if (action != KEYWORD_JOIN && action != KEYWORD_LEAVE) {
                throw KSLLogParsingException("Unknown queue action")
            }
            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) println("Queue Event: $action on $queueId")

        val queue = viewer.queues[queueId]
        val kslObject = viewer.objects[objectId] ?: throw RuntimeException("Object $objectId not found")

        if (queue != null) {
            when (action) {
                KEYWORD_JOIN -> queue.addObject(kslObject)
                KEYWORD_LEAVE -> queue.removeObject(kslObject)
                else -> throw RuntimeException("Unknown queue action")
            }
        } else {
            throw RuntimeException("Queue $queueId not found")
        }
    }
}
