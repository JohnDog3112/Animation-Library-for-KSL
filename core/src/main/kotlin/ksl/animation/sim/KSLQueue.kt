package ksl.animation.sim

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import ksl.animation.viewer.AnimationViewer
import kotlin.math.sqrt

class KSLQueue(id: String, private val startPosition: Position, private val endPosition: Position, private val scale: Double = 1.0) : KSLRenderable(id, (startPosition + endPosition) * 0.5) {
    constructor(queueObject: KSLAnimationObject.Queue) : this(queueObject.id, queueObject.startPosition, queueObject.endPosition, queueObject.scale)

    private val objects = mutableMapOf<String, KSLObject>()

    fun addObject(kslObject: KSLObject) {
        objects[kslObject.id] = kslObject
    }

    fun removeObject(kslObject: KSLObject) {
        objects.remove(kslObject.id)
    }

    override fun render(viewer: AnimationViewer) {
        // draw queue line
        viewer.shapeRenderer!!.begin(ShapeRenderer.ShapeType.Line)
        viewer.shapeRenderer!!.color = Color.BLACK
        Gdx.gl.glLineWidth((scale * 5).toFloat())
        val startX = startPosition.x * viewer.screenUnit
        val startY = startPosition.y * viewer.screenUnit
        val endX = endPosition.x * viewer.screenUnit
        val endY = endPosition.y * viewer.screenUnit
        viewer.shapeRenderer!!.line(
            (viewer.originX + startX).toFloat(),
            (viewer.originY + startY).toFloat(),
            (viewer.originX + endX).toFloat(),
            (viewer.originY + endY).toFloat()
        )

        // create line perpendicular to end point
        val dx = endX - startX
        val dy = endY - startY
        val length = sqrt(dx * dx + dy * dy).toFloat()
        val perpX = -dy / length * (10 * scale)
        val perpY = dx / length * (10 * scale)

        viewer.shapeRenderer!!.line(
            (viewer.originX + endX - perpX).toFloat(),
            (viewer.originY + endY - perpY).toFloat(),
            (viewer.originX + endX + perpX).toFloat(),
            (viewer.originY + endY + perpY).toFloat()
        )
        viewer.shapeRenderer!!.end()

        val normalizeX = dx / length
        val normalizeY = dy / length

        objects.entries.forEachIndexed { index, kslObject ->
            val width = kslObject.value.width * viewer.screenUnit
            val height = kslObject.value.height * viewer.screenUnit
            val x = endX - (width * normalizeX * index)
            val y = endY - (height * normalizeY * index)

            kslObject.value.render(x, y, width, height, viewer)
        }

        super.render(viewer)
    }
}
