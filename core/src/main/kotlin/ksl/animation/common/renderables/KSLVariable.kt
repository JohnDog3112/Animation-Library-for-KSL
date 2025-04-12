package ksl.animation.common.renderables

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisTextField
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ksl.animation.builder.changes.MoveAndResizeVariableChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ktx.actors.onChange
import ktx.actors.onExit
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


class KSLVariable(
    id: String,
    position: Position,
    var width: Double,
    var height: Double,
    defaultValue: String,
    private var maxTextScale: Double,
    private var precision: Int,
    private var textColor: Color,
) : KSLRenderable(id, position) {
    constructor(kslVariable: KSLAnimationObject.Variable) : this(kslVariable.id, kslVariable.position, kslVariable.width, kslVariable.height, kslVariable.defaultValue, kslVariable.maxTextScale, kslVariable.precision, Color.WHITE) {
        val colorNum = kslVariable.textColor
            .trim('#')
            .substring(0, 6)
            .padEnd(6, '0')
            .toInt(radix = 16)

        val red = colorNum.and(0xFF0000).shr(4*4)
        val green = colorNum.and(0x00FF00).shr(2*4)
        val blue = colorNum.and(0x0000FF)

        this.textColor = Color(red.toFloat()/255f, green.toFloat()/255f, blue.toFloat()/255f, 1.0f)

        this.recalculateClickPoints()
    }

    init {
        this.textColor = this.textColor.cpy()
    }

    fun serialize(): KSLAnimationObject.Variable {
        val colorNum = (
            (this.textColor.r*255 + 0.5).toInt().shl(8*2)
            + (this.textColor.g*255 + 0.5).toInt().shl(8)
            + (this.textColor.b*255 + 0.5).toInt()
        )
        return KSLAnimationObject.Variable(
            this.id,
            this.position,
            this.width,
            this.height,
            this.maxTextScale,
            this.value,
            this.precision,
            "#" + colorNum.toString(16)
        )
    }
    override fun displaySettings(content: VisTable, stage: Stage) {
        val variableIdTextField = VisTextField(id)
        variableIdTextField.onChange { id = variableIdTextField.text }
        content.add(VisLabel("Variable ID "))
        content.add(variableIdTextField)

        content.row()
        val defaultValueField = VisTextField(value)
        defaultValueField.onChange { value = this.text }
        defaultValueField.onExit {
            val asDouble = this.text.toDoubleOrNull()
            if (asDouble != null) {
                setValue(asDouble)
                this.text = value
            } else {
                setValue(this.text)
            }
        }
        content.add(VisLabel("Default Text"))
        content.add(defaultValueField)

        content.row()
        val maxTextScaleSpeed = SimpleFloatSpinnerModel(this.maxTextScale.toFloat(), 0.5f, 5f, 0.1f)
        val maxTextScaleSpinner = Spinner("Max Text Scale", maxTextScaleSpeed)
        maxTextScaleSpinner.onChange { maxTextScale = maxTextScaleSpeed.value.toDouble() }
        content.add(maxTextScaleSpinner)

        content.row()
        val precisionSpeed = SimpleFloatSpinnerModel(this.precision.toFloat(), 0.0f, 100f, 1f)
        val precisionSpinner = Spinner("Precision", precisionSpeed)
        precisionSpinner.onChange { precision = precisionSpeed.value.toInt() }
        content.add(precisionSpinner)

        content.row()
        val variableColorButton = VisTextButton("Variable Color")
        content.add(variableColorButton)
        variableColorButton.addListener(object: ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val colorPicker = ColorPicker("Variable Color", object : ColorPickerAdapter() {
                    override fun finished(color: Color) {
                        textColor = color
                    }
                })

                colorPicker.color = textColor
                stage.addActor(colorPicker)
            }
        })

        content.pack()
        super.displaySettings(content, stage)
    }
    private enum class DragPoint(val value: Int) {
        TOP(1),
        RIGHT(2),
        BOTTOM(4),
        LEFT(8),


        TOP_RIGHT(TOP.value.or(RIGHT.value)),
        BOTTOM_RIGHT(BOTTOM.value.or(RIGHT.value)),
        BOTTOM_LEFT(BOTTOM.value.or(LEFT.value)),
        TOP_LEFT(TOP.value.or(LEFT.value)),

        CENTER(16);

        fun sharesDirection(dragPoint: DragPoint): Boolean {
            return this.value.and(dragPoint.value) > 0
        }
    }
    private var clickPoints: MutableMap<DragPoint, Position> = mutableMapOf()
    private fun recalculateClickPoints() {
        clickPoints[DragPoint.TOP_LEFT] = this.position
        clickPoints[DragPoint.TOP] = this.position + Position(this.width/2.0, 0.0)
        clickPoints[DragPoint.TOP_RIGHT] = this.position + Position(this.width, 0.0)
        clickPoints[DragPoint.RIGHT] = this.position + Position(this.width, this.height/2.0)
        clickPoints[DragPoint.BOTTOM_RIGHT] = this.position + Position(this.width, this.height)
        clickPoints[DragPoint.BOTTOM] = this.position + Position(this.width/2.0, this.height)
        clickPoints[DragPoint.BOTTOM_LEFT] = this.position + Position(0.0, this.height)
        clickPoints[DragPoint.LEFT] = this.position + Position(0.0, this.height/2.0)

        clickPoints[DragPoint.CENTER] = this.position + Position(this.width/2.0, this.height/2.0)
    }

    private val layout: GlyphLayout = GlyphLayout()

    private val clickPointSize = 0.25

    private var value = defaultValue

    fun setValue(value: String) {
        this.value = value
    }
    fun setValue(value: Double) {
        val decimalFormat = DecimalFormat()
        decimalFormat.maximumFractionDigits = this.precision
        this.value = decimalFormat.format(value)
    }
    fun render(x: Double, y: Double, width: Double, height: Double, scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(Position(x, y))
        val translatedSize = Position(width, height) * scene.screenUnit

        scene.font.data.setScale(1f)
        this.layout.setText(scene.font, this.value)
        val scale = min(
            min(
                this.maxTextScale,
                translatedSize.x/this.layout.width
            ),
            translatedSize.y/this.layout.height
        )
        val textWidth = this.layout.width * scale
        val textHeight = this.layout.height * scale

        val textX = translatedPosition.x + (translatedSize.x - textWidth)/2.0
        val textY = translatedPosition.y + (translatedSize.y - textHeight)/2.0

        scene.font.color = this.textColor
        scene.font.data.setScale(scale.toFloat())

        scene.spriteBatch.begin()

        scene.font.draw(scene.spriteBatch, this.value, textX.toFloat(), textY.toFloat())
        scene.spriteBatch.end()

        scene.spriteBatch.begin()
        if (this.selected) {
            Gdx.gl.glLineWidth((0.1 * scene.screenUnit).toFloat())
            scene.shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            scene.shapeRenderer.color = Color.BLACK
            scene.shapeRenderer.rect(
                translatedPosition.x.toFloat(),
                translatedPosition.y.toFloat(),
                translatedSize.x.toFloat(),
                translatedSize.y.toFloat()
            )
            scene.shapeRenderer.end()
            Gdx.gl.glLineWidth(1f)

            this.recalculateClickPoints()
            val adjustedClickPointSize = (this.clickPointSize * scene.screenUnit).toFloat()
            scene.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            scene.shapeRenderer.color = Color.RED
            for ((_, pointPos) in this.clickPoints) {
                val adjustedPos = scene.worldToScreen(pointPos)
                scene.shapeRenderer.rect(
                    adjustedPos.x.toFloat() - adjustedClickPointSize/2f,
                    adjustedPos.y.toFloat() - adjustedClickPointSize/2f,
                    adjustedClickPointSize,
                    adjustedClickPointSize
                )
            }
            scene.shapeRenderer.end()
        }
        scene.spriteBatch.end()

    }

    override fun render(scene: AnimationScene) {
        this.render(this.position.x, this.position.y, this.width, this.height, scene)
    }

    override fun pointInside(scene: AnimationScene, point: Position): Boolean {
        val translatedStart = scene.worldToScreen(this.position)
        val translatedEnd = scene.worldToScreen(this.position + Position(this.width, this.height))

        val margin = 10
        val minX = min(translatedStart.x, translatedEnd.x) - margin
        val maxX = max(translatedStart.x, translatedEnd.x) + margin
        val minY = min(translatedStart.y, translatedEnd.y) - margin
        val maxY = max(translatedStart.y, translatedEnd.y) + margin

        return (point.x in minX..maxX && point.y in minY..maxY)
    }

    private var pointSelected: DragPoint? = null
    private var startPos: Position? = null
    private var startSize: Position? = null
    private var pointStartPos: Position? = null

    private fun handleDraggedPoint(unadjustedNewPos: Position, snapToGrid: Boolean) {
        if (this.pointSelected == null) return
        val point = this.pointSelected ?: throw RuntimeException("Internal Error!")
        val startSize = this.startSize ?: throw RuntimeException("Internal Error!")
        val pointStartPos = this.pointStartPos ?: throw RuntimeException("Internal Error!")

        var newPos = unadjustedNewPos
        if (snapToGrid) {
            newPos = Position(
                round(newPos.x),
                round(newPos.y)
            )
        }

        if (point == DragPoint.CENTER) {
            this.position = Position(
                newPos.x - this.width/2.0,
                newPos.y - this.height/2.0
            )
            return
        }

        if (point.sharesDirection(DragPoint.TOP)) {
            this.position.y = newPos.y
            this.height = startSize.y + pointStartPos.y - newPos.y
        } else if (point.sharesDirection(DragPoint.BOTTOM)) {
            this.height = startSize.y + newPos.y - pointStartPos.y
        }

        if (point.sharesDirection(DragPoint.LEFT)) {
            this.position.x = newPos.x
            this.width = startSize.x + pointStartPos.x - newPos.x
        } else if (point.sharesDirection(DragPoint.RIGHT)) {
            this.width = startSize.x + newPos.x - pointStartPos.x
        }
    }
    override fun onMouseDown(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) : Boolean {
        val translatedSize = Position(this.clickPointSize, this.clickPointSize) * scene.screenUnit
        this.recalculateClickPoints()
        for ((point, pointPos) in this.clickPoints) {
            val translatedPos = scene.worldToScreen(pointPos) - (translatedSize * 0.5)
            if (
                translatedPos.x <= x && x <= translatedPos.x + translatedSize.x
                && translatedPos.y <= y && y <= translatedPos.y + translatedSize.y
            ) {
                this.pointSelected = point
                this.startPos = Position(this.position.x, this.position.y)
                this.startSize = Position(this.width, this.height)
                this.pointStartPos = Position(pointPos.x, pointPos.y)

                this.handleDraggedPoint(scene.screenToWorld(Position(x.toDouble(), y.toDouble())), snapToGrid)

                return true
            }
        }
        return false
    }

    override fun onMouseUp(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) {
        if (this.pointSelected == null) return
        val startPos = this.startPos ?: throw RuntimeException("Internal Error!")
        val startSize = this.startSize ?: throw RuntimeException("Internal Error!")
        this.handleDraggedPoint(scene.screenToWorld(Position(x.toDouble(), y.toDouble())), snapToGrid)

        scene.applyChange(MoveAndResizeVariableChange(scene, id, startPos, startSize, this.position, Position(this.width, this.height)))

        this.pointSelected = null
        this.startPos = null
        this.startSize = null
        this.pointStartPos = null
    }

    override fun onMouseMove(scene: AnimationScene, x: Int, y: Int, snapToGrid: Boolean) {
        this.handleDraggedPoint(scene.screenToWorld(Position(x.toDouble(), y.toDouble())), snapToGrid)
    }
}
