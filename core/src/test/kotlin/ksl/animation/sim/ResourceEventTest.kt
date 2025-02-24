package ksl.animation.sim

import ksl.animation.sim.events.QueueEvent
import ksl.animation.sim.events.ResourceEvent
import ksl.animation.util.setupAnimationLogTest
import ksl.animation.viewer.AnimationViewer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ResourceEventTest {
    companion object {
        const val TEST_RESOURCE_ID = "test_resource"
        const val TEST_STATE = "test_state"

        private val animationViewer = AnimationViewer()

        @JvmStatic
        @BeforeAll
        fun setup() {
            setupAnimationLogTest(animationViewer, "/setup/resource_event.json")
        }
    }

    private fun parseLine(logLine: String): ResourceEvent {
        val event = ResourceEvent(0.0, animationViewer)
        val result = event.parse(logLine.split(" "))

        assertTrue(result)
        return event
    }

    @Test
    fun resourceEventTest() {
        val event = parseLine("RESOURCE \"$TEST_RESOURCE_ID\" SET STATE \"$TEST_STATE\"")
        assertEquals(event.resourceId, TEST_RESOURCE_ID)
        assertEquals(event.newState, TEST_STATE)
    }
}
