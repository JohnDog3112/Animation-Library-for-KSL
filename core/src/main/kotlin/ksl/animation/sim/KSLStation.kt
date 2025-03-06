package ksl.animation.sim

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.Main
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer

class KSLStation(id: String, position: Position): KSLRenderable(id, position) {
    constructor(stationObject: KSLAnimationObject.Station) : this(stationObject.id, stationObject.position)

    private val size = 0.25

    override fun render(viewer: AnimationViewer) {
        val x = position.x * viewer.screenUnit + viewer.originX
        val y = position.y * viewer.screenUnit + viewer.originY

        viewer.shapeRenderer!!.begin(ShapeRenderer.ShapeType.Filled)
        viewer.shapeRenderer!!.color = Color.RED
        viewer.shapeRenderer!!.rect((x - (size / 2 * viewer.screenUnit)).toFloat(), (y - (size / 2 * viewer.screenUnit)).toFloat(), (size * viewer.screenUnit).toFloat(), (size * viewer.screenUnit).toFloat())
        viewer.shapeRenderer!!.end()

        super.render(viewer)
    }
}
