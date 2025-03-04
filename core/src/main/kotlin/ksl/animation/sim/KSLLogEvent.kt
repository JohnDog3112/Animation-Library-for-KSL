package ksl.animation.sim

import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

abstract class KSLLogEvent(private val time: Double, protected val viewer: AnimationViewer) {
    open var containsObjects = false

    fun getTime(): Double {
        return time
    }

    private lateinit var tokens: List<String>
    private var currentToken = 0

    protected fun startParsing(tokens: List<String>) {
        currentToken = 0
        this.tokens = tokens
    }

    protected fun parseKeyword(): String {
        val keyword = tokens[currentToken]
        next()
        return keyword
    }

    protected fun parseObjectId(): String {
        val objectId = tokens[currentToken].trim('"')
        next()
        viewer.animationLog.objects.add(objectId)
        return objectId
    }

    protected fun parseObjectTypeId(): String {
        val objectTypeId = tokens[currentToken].trim('"')
        next()

        if (viewer.objectTypes[objectTypeId] != null) {
            return objectTypeId
        } else {
            throw KSLLogParsingException("Object type $objectTypeId not found")
        }
    }

    protected fun parseQueueId(): String {
        val queueId = tokens[currentToken].trim('"')
        next()

        if (viewer.queues[queueId] != null) {
            return queueId
        } else {
            throw KSLLogParsingException("Queue $queueId not found")
        }
    }

    protected fun parseResourceId(): String {
        val resourceId = tokens[currentToken].trim('"')
        next()

        if (viewer.resources[resourceId] != null) {
            return resourceId
        } else {
            throw KSLLogParsingException("Resource $resourceId not found")
        }
    }

    protected fun parseStation(): Position {
        val stationId = tokens[currentToken].trim('"')
        next()
        val station = viewer.stations[stationId] ?: throw KSLLogParsingException("Station $stationId not found")
        return station.position
    }

    protected fun parseString(): String {
        val value = tokens[currentToken].trim('"')
        next()
        return value
    }

    protected fun parseNumber(): Double {
        val value = tokens[currentToken]
        val number = value.toDoubleOrNull()
        next()
        return number ?: throw KSLLogParsingException("Invalid number: $value")
    }

    protected fun hasNext(): Boolean {
        return currentToken < tokens.size
    }

    protected fun next(amount: Int = 1) {
        currentToken += amount
    }

    abstract fun execute()
    abstract fun parse(tokens: List<String>): Boolean
    abstract fun involvesObject(objectId: String): Boolean
}

class KSLLogParsingException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
