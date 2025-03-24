package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Color
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import java.text.DecimalFormat

class KSLVariable(
    id: String,
    position: Position,
    defaultValue: String,
    private val textScale: Double,
    private val precision: Int,
) : KSLRenderable(id, position) {
    constructor(kslVariable: KSLAnimationObject.Variable) : this(kslVariable.id, kslVariable.position, kslVariable.defaultValue, kslVariable.textScale, kslVariable.precision)

    private var value = defaultValue

    fun setValue(value: String) {
        this.value = value
    }
    fun setValue(value: Double) {
        val decimalFormat = DecimalFormat()
        decimalFormat.maximumFractionDigits = this.precision
        this.value = decimalFormat.format(value)
    }
    fun render(x: Double, y: Double, scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(Position(x, y))


        scene.spriteBatch.begin()
        scene.font.color = Color.WHITE
        scene.font.data.setScale(this.textScale.toFloat())
        scene.font.draw(scene.spriteBatch, this.value, translatedPosition.x.toFloat(), translatedPosition.y.toFloat())
        scene.spriteBatch.end()
    }

    override fun render(scene: AnimationScene) {
        this.render(position.x, position.y, scene)
    }
}
