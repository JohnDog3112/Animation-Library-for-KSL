package ksl.animation.sim

import ksl.animation.sim.events.QueueEvent
import ksl.animation.util.setupAnimationLogTest
import ksl.animation.viewer.AnimationViewer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class QueueEventTest {
    companion object {
        const val TEST_QUEUE_ID = "test_queue"
        const val TEST_OBJECT_ID = "test_object"

        private val animationViewer = AnimationViewer()

        @JvmStatic
        @BeforeAll
        fun setup() {
            setupAnimationLogTest(animationViewer, "/setup/queue_event.json")
        }
    }

    private fun parseLine(logLine: String): QueueEvent {
        val event = QueueEvent(0.0, animationViewer)
        val result = event.parse(logLine.split(" "))

        assertTrue(result)
        return event
    }

    @Test
    fun queueEventTest1() {
        val event = parseLine("QUEUE \"$TEST_QUEUE_ID\" JOIN \"$TEST_OBJECT_ID\"")
        assertEquals(event.queueId, TEST_QUEUE_ID)
        assertEquals(event.objectId, TEST_OBJECT_ID)
        assertEquals(event.action, QueueEvent.KEYWORD_JOIN)
    }

    @Test
    fun queueEventTest2() {
        val event = parseLine("QUEUE \"$TEST_QUEUE_ID\" LEAVE \"$TEST_OBJECT_ID\"")
        assertEquals(event.queueId, TEST_QUEUE_ID)
        assertEquals(event.objectId, TEST_OBJECT_ID)
        assertEquals(event.action, QueueEvent.KEYWORD_LEAVE)
    }
}
