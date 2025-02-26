package ksl.animation.sim.events

import ksl.animation.sim.KSLAnimationLog
import ksl.animation.sim.KSLLogEvent
import ksl.animation.util.KSLAnimationGlobals
import ksl.animation.viewer.AnimationViewer

class ResourceEvent(time: Double, viewer: AnimationViewer, animationLog: KSLAnimationLog) : KSLLogEvent(time, viewer, animationLog) {
    companion object {
        const val KEYWORD_RESOURCE = "RESOURCE"
    }

    lateinit var resourceId: String
    lateinit var newState: String

    // RESOURCE <RESOURCE ID> SET STATE <NEW STATE>
    override fun parse(tokens: List<String>): Boolean {
        var currentToken = 0
        if (tokens[currentToken++] == KEYWORD_RESOURCE) {
            resourceId = tokens[currentToken++].trim('"')
            currentToken += 2
            newState = tokens[currentToken].trim('"')

            return true
        }
        return false
    }

    override fun execute() {
        if (KSLAnimationGlobals.VERBOSE) println("Resource Event: $newState on $resourceId")

        val resource = viewer.resources[resourceId] ?: throw RuntimeException("Resource $resourceId not found")
        resource.setState(newState)
    }

    override fun involvesObject(objectId: String): Boolean {
        return false
    }
}
