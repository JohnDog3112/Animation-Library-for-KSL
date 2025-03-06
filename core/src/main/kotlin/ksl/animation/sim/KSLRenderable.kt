package ksl.animation.sim

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import ksl.animation.Main
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

open class KSLRenderable(val id: String, var position: Position) {
    private val renderedId = GlyphLayout(Main.defaultFont, id)

    open fun render(viewer: AnimationViewer) {
        val x = position.x * viewer.screenUnit
        val y = position.y * viewer.screenUnit

        if (viewer.showIds) {
            viewer.spriteBatch!!.begin()
            Main.defaultFont.draw(viewer.spriteBatch!!, renderedId, (viewer.originX + x - renderedId.width / 2).toFloat(), (viewer.originY + y / 2).toFloat())
            viewer.spriteBatch!!.end()
        }
    }
}
