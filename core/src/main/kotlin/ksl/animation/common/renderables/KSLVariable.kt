package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextField
import com.kotcrab.vis.ui.widget.color.BasicColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ksl.animation.builder.changes.EditVariableSettingsChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ktx.actors.onChange
import ktx.actors.onExit
import java.text.DecimalFormat
import kotlin.math.min

class KSLVariable(
    id: String,
    position: Position,
    width: Double,
    height: Double,
    var defaultValue: String,
    var maxTextScale: Double,
    var precision: Int,
    var textColor: Color,
) : KSLResizable(id, position, width, height) {
    private val variableIdTextField = VisTextField(id)
    private val defaultValueField = VisTextField(defaultValue)
    private val maxTextScaleModel = SimpleFloatSpinnerModel(this.maxTextScale.toFloat(), 0.5f, 5f, 0.1f)
    private val maxTextScaleSpinner = Spinner("Max Text Scale", maxTextScaleModel)
    private val precisionModel = SimpleFloatSpinnerModel(this.precision.toFloat(), 0.0f, 100f, 1f)
    private val precisionSpinner = Spinner("Precision", precisionModel)
    private val colorPicker = BasicColorPicker()

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

    private val layout: GlyphLayout = GlyphLayout()
    private var value = defaultValue

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

    override fun openEditor(scene: AnimationScene, content: VisTable) {
        variableIdTextField.text = id
        val idTable = VisTable()
        idTable.add(VisLabel("Variable ID: "))
        idTable.add(variableIdTextField)
        content.add(idTable).row()

        defaultValueField.text = defaultValue
        val defaultValueTable = VisTable()
        defaultValueTable.add(VisLabel("Default Text: "))
        defaultValueTable.add(defaultValueField)
        content.add(defaultValueTable).row()

        maxTextScaleModel.value = maxTextScale.toFloat()
        content.add(maxTextScaleSpinner).row()
        precisionModel.value = precision.toFloat()
        content.add(precisionSpinner).row()

        colorPicker.color = textColor
        content.add(colorPicker).row()

        content.pack()
    }

    override fun closeEditor(scene: AnimationScene) {
        if (
            id != variableIdTextField.text ||
            defaultValue != defaultValueField.text ||
            maxTextScale != maxTextScaleModel.value.toDouble() ||
            precision != precisionModel.value.toInt() ||
            textColor != colorPicker.color
        ) {
            scene.applyChange(EditVariableSettingsChange(
                scene, this,
                id, variableIdTextField.text,
                defaultValue, defaultValueField.text,
                maxTextScale, maxTextScaleModel.value.toDouble(),
                precision, precisionModel.value.toInt(),
                textColor, colorPicker.color
            ))

            val asDouble = defaultValueField.text.toDoubleOrNull()
            if (asDouble != null) {
                setValue(asDouble)
                defaultValueField.text = value
            } else {
                setValue(defaultValueField.text)
            }
        }
    }

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

        super.render(scene)
    }

    override fun render(scene: AnimationScene) {
        this.render(this.position.x, this.position.y, this.width, this.height, scene)
    }
}
