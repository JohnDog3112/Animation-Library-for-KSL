package ksl.animation.common.renderables

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import ksl.animation.builder.changes.MoveResourceChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position
import kotlin.math.round

class KSLResource(id: String, position: Position, states: List<ResourceState>, private val width: Double = 1.0, private val height: Double = 1.0) : KSLRenderable(id, position) {
    constructor(resourceObject: KSLAnimationObject.Resource) : this(resourceObject.id, resourceObject.position, resourceObject.states, resourceObject.width, resourceObject.height)

    private val resourceStates = ResourceStates(states)
    private var currentState = resourceStates.defaultState.name
    private var dragging = false
    private var dragOffset = Position(0.0, 0.0)
    private var originalPosition: Position = position.copy()

    private fun distancePointToLine(point: Position, lineStart: Position, lineEnd: Position): Double {
        val A = point.x - lineStart.x
        val B = point.y - lineStart.y
        val C = lineEnd.x - lineStart.x
        val D = lineEnd.y - lineStart.y

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        val param = if (lenSq != 0.0) dot / lenSq else -1.0

        val (xx, yy) = when {
            param < 0 -> Pair(lineStart.x, lineStart.y)
            param > 1 -> Pair(lineEnd.x, lineEnd.y)
            else -> Pair(lineStart.x + param * C, lineStart.y + param * D)
        }

        val dx = point.x - xx
        val dy = point.y - yy
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun getClosestPointOnLine(point: Position, lineStart: Position, lineEnd: Position): Position {
        val A = point.x - lineStart.x
        val B = point.y - lineStart.y
        val C = lineEnd.x - lineStart.x
        val D = lineEnd.y - lineStart.y

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        val param = if (lenSq != 0.0) dot / lenSq else -1.0

        return when {
            param < 0 -> lineStart
            param > 1 -> lineEnd
            else -> Position(lineStart.x + param * C, lineStart.y + param * D)
        }
    }

    fun setState(state: String) {
        currentState = state
    }

    override fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = Position(width, height) * scene.screenUnit

        val texture: Texture = resourceStates.getImage(currentState, scene.images)
        scene.spriteBatch.begin()
        scene.spriteBatch.draw(
            texture,
            (translatedPosition.x - translatedSize.x / 2).toFloat(),
            (translatedPosition.y - translatedSize.y / 2).toFloat(),
            translatedSize.x.toFloat(),
            translatedSize.y.toFloat()
        )
        scene.spriteBatch.end()

        super.render(scene)
    }

    override fun pointInside(scene: AnimationScene, point: Position): Boolean {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = Position(width, height) * scene.screenUnit
        val halfWidth = translatedSize.x / 2
        val halfHeight = translatedSize.y / 2
        return point.x in (translatedPosition.x - halfWidth)..(translatedPosition.x + halfWidth) &&
            point.y in (translatedPosition.y - halfHeight)..(translatedPosition.y + halfHeight)
    }

    override fun onMouseDown(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean): Boolean {
        val mousePos = Position(x.toDouble(), y.toDouble())
        if (button == Input.Buttons.LEFT && pointInside(scene, mousePos)) {
            dragging = true
            originalPosition = position.copy() // Record starting position for the move command
            dragOffset = mousePos - scene.worldToScreen(position)
            return true
        }
        return false
    }

    override fun onMouseMove(scene: AnimationScene, x: Int, y: Int, snapToGrid: Boolean) {
        if (dragging) {
            val mousePos = Position(x.toDouble(), y.toDouble())
            val newScreenPos = mousePos - dragOffset
            val newWorldPos = scene.screenToWorld(newScreenPos)
            position.x = if (snapToGrid) round(newWorldPos.x) else newWorldPos.x
            position.y = if (snapToGrid) round(newWorldPos.y) else newWorldPos.y
        }
    }

    override fun onMouseUp(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) {
        if (dragging) {
            dragging = false

            val resourceScreenPos = scene.worldToScreen(position)

            scene.queues.values.forEach { queue: KSLQueue ->
                val translatedStart = scene.worldToScreen(queue.startPosition)
                val translatedEnd = scene.worldToScreen(queue.endPosition)
                val distance = distancePointToLine(resourceScreenPos, translatedStart, translatedEnd)

            }
            scene.applyChange(MoveResourceChange(scene, id, originalPosition, position.copy()))
        }

    }
}
