package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.Main

class AnimationBuilder {
    var spriteBatch: SpriteBatch? = null
    var shapeRenderer: ShapeRenderer? = null

    var screenUnit = 0.0
    var originX = 0.0
    var originY = 0.0
    var showGridLines = true

    fun render(delta: Float) {
        if (spriteBatch == null) spriteBatch = SpriteBatch()
        if (shapeRenderer == null) shapeRenderer = ShapeRenderer()

        spriteBatch!!.transformMatrix = Main.camera.view
        shapeRenderer!!.transformMatrix = Main.camera.view
        spriteBatch!!.projectionMatrix = Main.camera.projection
        shapeRenderer!!.projectionMatrix = Main.camera.projection

        screenUnit = Main.camera.viewportWidth / 10.0
        originX = Main.camera.viewportWidth / 2.0
        originY = Main.camera.viewportHeight / 2.0

//        shapeRenderer!!.begin(ShapeRenderer.ShapeType.Line)
//        shapeRenderer!!.color = Color.WHITE
//        Gdx.gl.glLineWidth(3f)
//        for (i in -5..10) {
//            shapeRenderer!!.line((i * screenUnit).toFloat(), 0f, (i * screenUnit).toFloat(), Main.camera.viewportHeight)
//            shapeRenderer!!.line((i * screenUnit).toFloat(), 0f, (i * screenUnit).toFloat(), Main.camera.viewportHeight)
//        }
//        shapeRenderer!!.end()
    }
}
