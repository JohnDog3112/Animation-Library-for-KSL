package ksl.animation.common

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.Main
import ksl.animation.common.renderables.*
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.events.MoveQuery
import ksl.animation.util.Position
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

open class AnimationScene {
    val images = mutableMapOf<String, Texture>()
    val objectTypes = mutableMapOf<String, KSLAnimationObject.ObjectType>()
    val movements = mutableListOf<MoveQuery>()

    val objects = mutableMapOf<String, KSLObject>()
    val queues = mutableMapOf<String, KSLQueue>()
    val resources = mutableMapOf<String, KSLResource>()
    val stations = mutableMapOf<String, KSLStation>()
    val renderables = mutableMapOf<String, KSLRenderable>()

    var spriteBatch: SpriteBatch = SpriteBatch()
    var shapeRenderer: ShapeRenderer = ShapeRenderer()

    private var offset = Position(0.0, 0.0)
    private var startPan = Position(0.0, 0.0)
    private var screenAmount = 20.0
    var screenUnit = 0.0
    var showGridLines = true
    var showIds = false

    private val changes = mutableMapOf<Int, AnimationChange>()
    private var changePointer = 0
    private var canRedo = false
    private var redoPointer = 0

    init {
        offset = Position(Main.camera.viewportWidth / 2.0, Main.camera.viewportHeight / 2.0)
    }

    fun worldToScreen(position: Position): Position {
        return (position * screenUnit) + offset
    }

    fun screenToWorld(position: Position): Position {
        return (position - offset) * (1.0 / screenUnit)
    }

    fun addRenderable(queue: KSLQueue) {
        queues[queue.id] = queue
        renderables[queue.id] = queue
    }

    fun addRenderable(kslObject: KSLObject) {
        objects[kslObject.id] = kslObject
        renderables[kslObject.id] = kslObject
    }

    fun addRenderable(resource: KSLResource) {
        resources[resource.id] = resource
        renderables[resource.id] = resource
    }

    fun addRenderable(station: KSLStation) {
        stations[station.id] = station
        renderables[station.id] = station
    }

    open fun render(delta: Float) {
        val mouse = Position(Gdx.input.x.toDouble(), Gdx.input.y.toDouble())

        if (Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE)) {
            startPan = mouse
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            offset += (mouse - startPan)
            startPan = mouse
        }

        val min = screenToWorld(Position(0.0, 0.0))
        val max = screenToWorld(Position(Main.camera.viewportWidth.toDouble(), Main.camera.viewportHeight.toDouble()))

        if (showGridLines) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.WHITE

            for (i in floor(min.x).toInt()..ceil(max.x).toInt()) {
                val minTranslated = worldToScreen(Position(i.toDouble(), floor(min.y)))
                val maxTranslated = worldToScreen(Position(i.toDouble(), ceil(max.y)))
                shapeRenderer.rectLine(minTranslated.x.toFloat(), minTranslated.y.toFloat(), maxTranslated.x.toFloat(), maxTranslated.y.toFloat(), if (i == 0) 5f else 2f)
            }

            for (i in floor(min.y).toInt()..ceil(max.y).toInt()) {
                val minTranslated = worldToScreen(Position(floor(min.x), i.toDouble()))
                val maxTranslated = worldToScreen(Position(ceil(max.x), i.toDouble()))
                shapeRenderer.rectLine(minTranslated.x.toFloat(), minTranslated.y.toFloat(), maxTranslated.x.toFloat(), maxTranslated.y.toFloat(), if (i == 0) 5f else 2f)
            }
            shapeRenderer.end()
        }
    }

    fun applyChange(change: AnimationChange) {
        change.apply()
        changes[changePointer] = change
        changePointer++
        canRedo = false
    }

    fun undo() {
        changePointer--
        if (changePointer < 0) {
            changePointer = 0
        } else {
            changes[changePointer]?.undo()
            if (!canRedo) {
                canRedo = true
                redoPointer = changePointer + 1
            }
        }
    }

    fun redo() {
        if (canRedo && changePointer < redoPointer) {
            changes[changePointer]?.redo()
            changePointer++

            if (changePointer == redoPointer) canRedo = false
        }
    }

    fun resize() {
        spriteBatch.transformMatrix = Main.camera.view
        shapeRenderer.transformMatrix = Main.camera.view
        spriteBatch.projectionMatrix = Main.camera.projection
        shapeRenderer.projectionMatrix = Main.camera.projection

        screenUnit = Main.camera.viewportWidth / screenAmount
    }

    open fun dispose() {
        shapeRenderer.dispose()
        spriteBatch.dispose()
    }
}
