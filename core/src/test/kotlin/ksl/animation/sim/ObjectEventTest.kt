package ksl.animation.sim

import ksl.animation.sim.events.ObjectEvent
import ksl.animation.util.Position
import ksl.animation.util.setupAnimationLogTest
import ksl.animation.viewer.AnimationViewer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ObjectEventTest {
    companion object {
        const val TEST_OBJECT_TYPE = "test_object_type"
        const val TEST_OBJECT_ID = "test_object"
        const val TEST_WIDTH = 1.5
        const val TEST_HEIGHT = 1.7
        const val TEST_STATION = "test_station"
        val TEST_POSITION = Position(1.1, 1.3)

        private val animationViewer = AnimationViewer()

        @JvmStatic
        @BeforeAll
        fun setup() {
            setupAnimationLogTest(animationViewer, "/setup/spawn_event.json")
        }
    }

    private fun parseLine(logLine: String): ObjectEvent {
        val event = ObjectEvent(0.0, animationViewer)
        val result = event.parse(logLine.split(" "))

        assertTrue(result)
        return event
    }

    @Test
    fun spawnEventTest1() {
        val event = parseLine("OBJECT ADD \"$TEST_OBJECT_TYPE\" AS \"$TEST_OBJECT_ID\"")
        assertEquals(event.action, ObjectEvent.KEYWORD_ADD)
        assertEquals(event.objectTypeId, TEST_OBJECT_TYPE)
        assertEquals(event.objectId, TEST_OBJECT_ID)
    }

    @Test
    fun spawnEventTest2() {
        val event = parseLine("OBJECT ADD \"$TEST_OBJECT_TYPE\" AS \"$TEST_OBJECT_ID\" AT \"$TEST_STATION\"")
        assertEquals(event.action, ObjectEvent.KEYWORD_ADD)
        assertEquals(event.objectTypeId, TEST_OBJECT_TYPE)
        assertEquals(event.objectId, TEST_OBJECT_ID)
        assertEquals(event.position, TEST_POSITION)
    }

    @Test
    fun spawnEventTest3() {
        val event = parseLine("OBJECT ADD \"$TEST_OBJECT_TYPE\" AS \"$TEST_OBJECT_ID\" SIZED $TEST_WIDTH $TEST_HEIGHT")
        assertEquals(event.action, ObjectEvent.KEYWORD_ADD)
        assertEquals(event.objectTypeId, TEST_OBJECT_TYPE)
        assertEquals(event.objectId, TEST_OBJECT_ID)
        assertEquals(event.width, TEST_WIDTH)
        assertEquals(event.height, TEST_HEIGHT)
    }

    @Test
    fun spawnEventTest4() {
        val event = parseLine("OBJECT ADD \"$TEST_OBJECT_TYPE\" AS \"$TEST_OBJECT_ID\" AT \"$TEST_STATION\" SIZED $TEST_WIDTH $TEST_HEIGHT")
        assertEquals(event.action, ObjectEvent.KEYWORD_ADD)
        assertEquals(event.objectTypeId, TEST_OBJECT_TYPE)
        assertEquals(event.objectId, TEST_OBJECT_ID)
        assertEquals(event.position, TEST_POSITION)
        assertEquals(event.width, TEST_WIDTH)
        assertEquals(event.height, TEST_HEIGHT)
    }

    @Test
    fun spawnEventTest5() {
        val event = parseLine("OBJECT REMOVE \"$TEST_OBJECT_ID\"")
        assertEquals(event.action, ObjectEvent.KEYWORD_REMOVE)
        assertEquals(event.objectId, TEST_OBJECT_ID)
    }
}
