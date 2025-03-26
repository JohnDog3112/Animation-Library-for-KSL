package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import java.text.DecimalFormat
import kotlin.math.min

class KSLVariable(
    id: String,
    position: Position,
    val width: Double,
    val height: Double,
    defaultValue: String,
    private val maxTextScale: Double,
    private val precision: Int,
    private var color: Color,
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

        this.color = Color(red.toFloat()/255f, green.toFloat()/255f, blue.toFloat()/255f, 1.0f)
    }

    private val layout: GlyphLayout = GlyphLayout()

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

        scene.font.color = this.color
        scene.font.data.setScale(scale.toFloat())

        scene.spriteBatch.begin()
        scene.font.draw(scene.spriteBatch, this.value, textX.toFloat(), textY.toFloat())
        scene.spriteBatch.end()
    }

    override fun render(scene: AnimationScene) {
        this.render(this.position.x, this.position.y, this.width, this.height, scene)
    }
}
