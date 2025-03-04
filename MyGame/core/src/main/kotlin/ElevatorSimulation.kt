package com.example.mygame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Queue
import kotlin.random.Random

data class FloorRequest(val floor: Int, val directionUp: Boolean)

class Elevator(var currentFloor: Int = 0)

data class Box(var startFloor: Int, var targetFloor: Int, val color: Color)

class ElevatorSimulation : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var shapeRenderer: ShapeRenderer

    private val floors = List(16) { it } // 16 floors numbered 0 to 15
    private val elevator = Elevator()
    private val eventQueue = Queue<FloorRequest>() // Queue to hold floor requests
    private val boxes = mutableListOf<Box>() // List to hold boxes in the elevator
    private val waitingBoxes = mutableMapOf<Int, MutableList<Box>>() // Boxes waiting on each floor

    private val floorHeight = 40f
    private val elevatorWidth = 40f
    private val elevatorHeight = floorHeight - 5f
    private var requestTimer = 2f // Timer to periodically add random floor requests

    override fun create() {
        batch = SpriteBatch()
        font = BitmapFont()
        shapeRenderer = ShapeRenderer()
        font.color = Color.WHITE

        // Add some initial boxes with random target floors
        for (i in 0 until 5) {
            addRandomBox()
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val deltaTime = Gdx.graphics.deltaTime
        requestTimer -= deltaTime

        // Periodically generate random requests
        if (requestTimer <= 0f) {
            addRandomRequest()
            addRandomBox()
            requestTimer = 2f // Reset the timer for the next request
        }

        // Process the next event in the queue if available
        processNextEvent()

        // Draw floors, elevator, requests, and boxes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (floor in floors) {
            val yPosition = floorHeight * floor + 30

            // Draw floor as a horizontal line
            shapeRenderer.color = Color.DARK_GRAY
            shapeRenderer.rect(0f, yPosition, Gdx.graphics.width.toFloat(), 2f)

            // Draw elevator if it's on this floor
            if (elevator.currentFloor == floor) {
                shapeRenderer.color = Color.CYAN
                shapeRenderer.rect(100f, yPosition - elevatorHeight / 2, elevatorWidth, elevatorHeight)

                // Draw boxes inside the elevator
                var boxOffset = 0f
                for (box in boxes) {
                    shapeRenderer.color = box.color
                    shapeRenderer.rect(110f + boxOffset, yPosition - elevatorHeight / 2 + 5f, 10f, 10f)
                    boxOffset += 15f
                }
            }

            // Draw request indicators
            for (request in eventQueue) {
                if (request.floor == floor) {
                    shapeRenderer.color = if (request.directionUp) Color.GREEN else Color.RED
                    shapeRenderer.rect(20f, yPosition - 10f, 10f, 10f) // Small box indicator
                }
            }

            // Draw boxes waiting on each floor
            waitingBoxes[floor]?.let { floorBoxes ->
                var boxOffset = 0f
                for (box in floorBoxes) {
                    shapeRenderer.color = box.color
                    shapeRenderer.rect(50f + boxOffset, yPosition - 10f, 10f, 10f)
                    boxOffset += 15f
                }
            }
        }

        shapeRenderer.end()

        // Draw floor numbers
        batch.begin()
        for (floor in floors) {
            val yPosition = floorHeight * floor + 30
            font.draw(batch, "Floor $floor", 5f, yPosition + 10)
        }
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        shapeRenderer.dispose()
    }

    // Adds a random floor request (either up or down) to the event queue
    private fun addRandomRequest() {
        val floor = Random.nextInt(floors.size)
        val directionUp = Random.nextBoolean()
        eventQueue.addLast(FloorRequest(floor, directionUp))
    }

    // Adds a random box with a target floor
    private fun addRandomBox() {
        val startFloor = Random.nextInt(floors.size)
        var targetFloor: Int
        do {
            targetFloor = Random.nextInt(floors.size)
        } while (targetFloor == startFloor) // Ensure the box has a different target floor

        val color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
        val box = Box(startFloor, targetFloor, color)

        // Add the box to the waiting list on its start floor
        waitingBoxes.getOrPut(startFloor) { mutableListOf() }.add(box)
    }

    // Processes the next event in the queue, moving the elevator and handling boxes
    private fun processNextEvent() {
        if (eventQueue.isEmpty) return // No events to process

        val nextRequest = eventQueue.first()

        // Move elevator towards the requested floor
        when {
            elevator.currentFloor < nextRequest.floor -> elevator.currentFloor++
            elevator.currentFloor > nextRequest.floor -> elevator.currentFloor--
            else -> {
                // Elevator has reached the requested floor
                eventQueue.removeFirst() // Remove the completed request

                // Load boxes from the waiting area onto the elevator
                waitingBoxes[elevator.currentFloor]?.let { floorBoxes ->
                    boxes.addAll(floorBoxes)
                    floorBoxes.clear()
                }

                // Unload boxes that have reached their target floor
                boxes.removeAll { box ->
                    if (box.targetFloor == elevator.currentFloor) {
                        waitingBoxes.getOrPut(elevator.currentFloor) { mutableListOf() }.add(box)
                        true // Remove from the elevator
                    } else {
                        false
                    }
                }
            }
        }
    }
}
