package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.kotcrab.vis.ui.widget.VisTable
import ksl.animation.Main
import ksl.animation.common.AnimationScene
import ksl.animation.util.Position

open class KSLRenderable(var id: String, var position: Position) {
    private var renderedId = GlyphLayout(Main.defaultFont, id)
    var highlighted = false
    var selected = false

    open fun openEditor(scene: AnimationScene, content: VisTable) {}
    open fun closeEditor(scene: AnimationScene) {}

    open fun pointInside(scene: AnimationScene, point: Position): Boolean {
        return false
    }

    open fun onMouseDown(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) : Boolean { return false }
    open fun onMouseUp(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) {}
    open fun onMouseMove(scene: AnimationScene, x: Int, y: Int, snapToGrid: Boolean) {}

    open fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)

        if (scene.showIds) {
            renderedId = GlyphLayout(Main.defaultFont, id)

            scene.spriteBatch.begin()
            Main.defaultFont.draw(scene.spriteBatch, renderedId, (translatedPosition.x - renderedId.width / 2).toFloat(), (translatedPosition.y + renderedId.height).toFloat(), )
            scene.spriteBatch.end()
        }
    }
}
