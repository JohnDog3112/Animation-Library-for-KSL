package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position

class KSLStation(id: String, position: Position): KSLRenderable(id, position) {
    constructor(stationObject: KSLAnimationObject.Station) : this(stationObject.id, stationObject.position)

    override fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = 0.25 * scene.screenUnit

        scene.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        scene.shapeRenderer.color = Color.RED
        scene.shapeRenderer.rect((translatedPosition.x - translatedSize / 2).toFloat(), (translatedPosition.y - translatedSize / 2).toFloat(), translatedSize.toFloat(), translatedSize.toFloat())
        scene.shapeRenderer.end()

        super.render(scene)
    }
}
