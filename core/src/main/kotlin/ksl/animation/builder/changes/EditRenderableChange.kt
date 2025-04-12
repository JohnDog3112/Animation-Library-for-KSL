package ksl.animation.builder.changes

import com.badlogic.gdx.graphics.Color
import com.kotcrab.vis.ui.widget.VisTextField
import com.kotcrab.vis.ui.widget.color.BasicColorPicker
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import ksl.animation.common.AnimationChange
import ksl.animation.common.AnimationScene
import ksl.animation.common.renderables.KSLQueue
import ksl.animation.common.renderables.KSLResource
import ksl.animation.common.renderables.KSLStation
import ksl.animation.setup.ResourceState
import ksl.animation.common.renderables.KSLVariable
import ksl.animation.util.Position

class EditQueueSettingsChange(
    scene: AnimationScene,
    private val queue: KSLQueue,
    private val previousId: String,
    private val id: String,
    private val previousScale: Double,
    private val scale: Double
) : AnimationChange(scene) {
    override fun apply() {
        queue.id = id
        queue.scale = scale
    }

    override fun redo() {
        queue.id = id
        queue.scale = scale
    }

    override fun undo() {
        queue.id = previousId
        queue.scale = previousScale
    }
}

class EditResourceSettingsChange(
    scene: AnimationScene,
    private val resource: KSLResource,
    private val previousId: String,
    private val id: String
) : AnimationChange(scene) {
    override fun apply() {
        resource.id = id
    }

    override fun redo() {
        resource.id = id
    }

    override fun undo() {
        resource.id = previousId
    }
}

class EditStationSettingsChange(
    scene: AnimationScene,
    private val station: KSLStation,
    private val previousId: String,
    private val id: String
) : AnimationChange(scene) {
    override fun apply() {
        station.id = id
    }

    override fun redo() {
        station.id = id
    }

    override fun undo() {
        station.id = previousId
    }
}

class EditVariableSettingsChange(
    scene: AnimationScene,
    private val variable: KSLVariable,
    private val previousId: String,
    private val id: String,
    private val previousDefaultValue: String,
    private val defaultValue: String,
    private val previousMaxTextScale: Double,
    private val maxTextScale: Double,
    private val previousPrecision: Int,
    private val precision: Int,
    private val previousTextColor: Color,
    private val textColor: Color
) : AnimationChange(scene) {
    override fun apply() {
        variable.id = id
        variable.defaultValue = defaultValue
        variable.maxTextScale = maxTextScale
        variable.precision = precision
        variable.textColor = textColor
    }

    override fun redo() {
        variable.id = id
        variable.defaultValue = defaultValue
        variable.maxTextScale = maxTextScale
        variable.precision = precision
        variable.textColor = textColor
    }

    override fun undo() {
        variable.id = previousId
        variable.defaultValue = previousDefaultValue
        variable.maxTextScale = previousMaxTextScale
        variable.precision = previousPrecision
        variable.textColor = previousTextColor
    }
}
