package ksl.animation.common.renderables

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ksl.animation.builder.changes.MoveStationChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.util.Position
import kotlin.math.round

class KSLStation(id: String, position: Position): KSLRenderable(id, position) {
    constructor(stationObject: KSLAnimationObject.Station) : this(stationObject.id, stationObject.position)

    private var dragging = false
    private var dragOffset = Position(0.0, 0.0)
    private var originalPosition: Position = position.copy()

    override fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = 0.25 * scene.screenUnit

        scene.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        scene.shapeRenderer.color = Color.RED
        scene.shapeRenderer.rect((translatedPosition.x - translatedSize / 2).toFloat(), (translatedPosition.y - translatedSize / 2).toFloat(), translatedSize.toFloat(), translatedSize.toFloat())
        scene.shapeRenderer.end()

        super.render(scene)
    }

    override fun pointInside(scene: AnimationScene, point: Position): Boolean {
        val translatedPosition = scene.worldToScreen(position)
        val size = 0.25 * scene.screenUnit
        val halfSize = size / 2
        return point.x >= (translatedPosition.x - halfSize) &&
            point.x <= (translatedPosition.x + halfSize) &&
            point.y >= (translatedPosition.y - halfSize) &&
            point.y <= (translatedPosition.y + halfSize)
    }

    override fun onMouseDown(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean): Boolean {
        val mousePos = Position(x.toDouble(), y.toDouble())
        if (button == Input.Buttons.LEFT && pointInside(scene, mousePos)) {
            dragging = true
            originalPosition = position.copy()
            dragOffset = mousePos - scene.worldToScreen(position)
            return true
        }
        return false
    }

    override fun onMouseMove(scene: AnimationScene, x: Int, y: Int, snapToGrid: Boolean) {
        if (dragging) {
            val mousePos = Position(x.toDouble(), y.toDouble())
            val newScreenPos = mousePos - dragOffset
            val newWorldPos = scene.screenToWorld(newScreenPos)
            position.x = if (snapToGrid) round(newWorldPos.x) else newWorldPos.x
            position.y = if (snapToGrid) round(newWorldPos.y) else newWorldPos.y
        }
    }

    override fun onMouseUp(scene: AnimationScene, x: Int, y: Int, button: Int, snapToGrid: Boolean) {
        if (dragging) {
            dragging = false
            scene.applyChange(MoveStationChange(scene, id, originalPosition, position.copy()))
        }
    }

}
