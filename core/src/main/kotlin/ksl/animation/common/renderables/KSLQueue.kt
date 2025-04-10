package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisTextField
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ksl.animation.builder.ObjectEditorWindow
import ksl.animation.builder.changes.EditQueueSettings
import ksl.animation.builder.changes.MoveQueueChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ktx.actors.onChange
import ktx.actors.onClick
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt

class KSLQueue(id: String, var startPosition: Position, var endPosition: Position, var scale: Double = 1.0) : KSLRenderable(id, (startPosition + endPosition) * 0.5) {
    constructor(queueObject: KSLAnimationObject.Queue) : this(queueObject.id, queueObject.startPosition, queueObject.endPosition, queueObject.scale)

    fun serialize(): KSLAnimationObject.Queue {
        return KSLAnimationObject.Queue(
            this.id,
            this.startPosition,
            this.endPosition,
            this.scale
        )
    }

    companion object {
        const val START_POINT = "START"
        const val END_POINT = "END"
        const val BOTH_POINTS = "BOTH"
    }

    private val objects = mutableMapOf<String, KSLObject>()
    private var pointSelected = ""
    private var actionStart = Position(0.0, 0.0)
    private var actionEnd = Position(0.0, 0.0)
    private var pointOffset = Position(0.0, 0.0)

    fun addObject(kslObject: KSLObject) {
        objects[kslObject.id] = kslObject
        kslObject.inQueue = true
    }

    fun removeObject(kslObject: KSLObject) {
        objects.remove(kslObject.id)
        kslObject.inQueue = false
    }

    override fun displaySettings(scene: AnimationScene, content: VisTable) {
        val queueIdTextField = VisTextField(id)
        queueIdTextField.onChange { scene.applyChange(EditQueueSettings(scene, this@KSLQueue, id, queueIdTextField.text, scale, scale)) }

        val speedModel = SimpleFloatSpinnerModel(scale.toFloat(), 0.5f, 5f, 0.1f)
        val spinner = Spinner("Scale", speedModel)
        spinner.onChange { scene.applyChange(EditQueueSettings(scene, this@KSLQueue, id, id, scale, speedModel.value.toDouble())) }

        val idTable = VisTable()
        idTable.add(VisLabel("Queue ID: "))
        idTable.add(queueIdTextField).width(100f)

        content.add(idTable).row()
        content.add(spinner).row()
        super.displaySettings(scene, content)
    }

    override fun pointInside(scene: AnimationScene, point: Position): Boolean {
        val translatedStart = scene.worldToScreen(startPosition)
        val translatedEnd = scene.worldToScreen(endPosition)

        val margin = 10
        val minX = min(translatedStart.x, translatedEnd.x) - margin
        val maxX = max(translatedStart.x, translatedEnd.x) + margin
        val minY = min(translatedStart.y, translatedEnd.y) - margin
        val maxY = max(translatedStart.y, translatedEnd.y) + margin

        return (point.x in minX..maxX && point.y in minY..maxY)
    }

    override fun onMouseDown(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) : Boolean {
        // for undoing/redoing
        actionStart = startPosition
        actionEnd = endPosition

        val translatedStart = scene.worldToScreen(startPosition)
        val translatedEnd = scene.worldToScreen(endPosition)
        val midpoint = (startPosition + endPosition) * 0.5
        val translatedMidpoint = scene.worldToScreen(midpoint)
        val size = scale * 5
        val mouse = Position(x.toDouble(), y.toDouble())

        // starting point move
        if (mouse.x >= translatedStart.x - size && mouse.x <= translatedStart.x + size && mouse.y >= translatedStart.y - size && mouse.y <= translatedStart.y + size) {
            pointSelected = START_POINT
            startPosition = scene.screenToWorld(mouse)
            return true
        }

        // ending point move
        if (mouse.x >= translatedEnd.x - size && mouse.x <= translatedEnd.x + size && mouse.y >= translatedEnd.y - size && mouse.y <= translatedEnd.y + size) {
            pointSelected = END_POINT
            endPosition = scene.screenToWorld(mouse)
            return true
        }

        // both point move
        if (mouse.x >= translatedMidpoint.x - size && mouse.x <= translatedMidpoint.x + size && mouse.y >= translatedMidpoint.y - size && mouse.y <= translatedMidpoint.y + size) {
            pointSelected = BOTH_POINTS
            pointOffset = endPosition - midpoint
            return true
        }

        return false
    }

    override fun onMouseUp(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) {
        scene.applyChange(MoveQueueChange(scene, id, actionStart, actionEnd, startPosition, endPosition))
        pointSelected = ""
    }

    override fun onMouseMove(scene: AnimationScene, x: Int, y: Int, snapToGrid: Boolean) {
        val position = scene.screenToWorld(Position(x.toDouble(), y.toDouble()))
        if (snapToGrid) {
            position.x = round(position.x)
            position.y = round(position.y)
        }

        when (pointSelected) {
            START_POINT -> startPosition = position
            END_POINT -> endPosition = position
            BOTH_POINTS -> {
                startPosition = position - pointOffset
                endPosition = position + pointOffset
            }
        }
    }

    override fun render(scene: AnimationScene) {
        // update midpoint
        position = (startPosition + endPosition) * (1.0 / 2.0)

        // draw queue line
        scene.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        scene.shapeRenderer.color = if (highlighted || selected) Color.GRAY else Color.BLACK

        val translatedStart = scene.worldToScreen(startPosition)
        val translatedEnd = scene.worldToScreen(endPosition)
        val midpoint = (translatedStart + translatedEnd) * (1.0 / 2.0)
        val size = scale * 5

        scene.shapeRenderer.rectLine(
            translatedStart.x.toFloat(),
            translatedStart.y.toFloat(),
            translatedEnd.x.toFloat(),
            translatedEnd.y.toFloat(),
            size.toFloat()
        )

        // create line perpendicular to end point
        val dist = translatedEnd - translatedStart
        val length = sqrt(dist.x * dist.x + dist.y * dist.y).toFloat()
        val perpX = -dist.y / length * (scale * 10)
        val perpY = dist.x / length * (scale * 10)

        scene.shapeRenderer.rectLine(
            (translatedEnd.x - perpX).toFloat(),
            (translatedEnd.y - perpY).toFloat(),
            (translatedEnd.x + perpX).toFloat(),
            (translatedEnd.y + perpY).toFloat(),
            size.toFloat()
        )

        // if selected, draw points
        if (selected) {
            scene.shapeRenderer.color = Color.RED
            scene.shapeRenderer.rect((translatedStart.x - size).toFloat(), (translatedStart.y - size).toFloat(), (size * 2).toFloat(), (size * 2).toFloat())
            scene.shapeRenderer.rect((midpoint.x - size).toFloat(), (midpoint.y - size).toFloat(), (size * 2).toFloat(), (size * 2).toFloat())
            scene.shapeRenderer.rect((translatedEnd.x - size).toFloat(), (translatedEnd.y - size).toFloat(), (size * 2).toFloat(), (size * 2).toFloat())
        }

        scene.shapeRenderer.end()

        // render objects in the queue
        val normalizeX = dist.x / length
        val normalizeY = dist.y / length

        objects.entries.forEachIndexed { index, kslObject ->
            val x = endPosition.x - (kslObject.value.width * normalizeX * index)
            val y = endPosition.y - (kslObject.value.height * normalizeY * index)

            kslObject.value.render(x, y, kslObject.value.width, kslObject.value.height, scene)
        }

        super.render(scene)
    }
}
