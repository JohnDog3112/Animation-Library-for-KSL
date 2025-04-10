package ksl.animation.builder

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.*
import ksl.animation.Assets.getUITexture
import ktx.actors.onClick
import ktx.scene2d.vis.visTextTooltip

class AddObjectWindow(onObjectAdd: (type: String) -> Unit) : VisWindow("Add Object", false) {
    private var queueIcon: TextureRegion = getUITexture(2, 0)
    private var resourceIcon: TextureRegion = getUITexture(3, 0)
    private var stationIcon: TextureRegion = getUITexture(4, 0)
    private var variableIcon: TextureRegion = getUITexture(5, 0)
    private var open = false

    init {
        titleLabel.setAlignment(Align.center)
        addCloseButton()

        defaults().pad(20f, 40f, 20f, 40f)

        isMovable = false
        isKeepWithinParent = false
        setKeepWithinStage(false)

        val queueButton = VisImageButton(TextureRegionDrawable(queueIcon))
        queueButton.visTextTooltip("Add Queue")
        queueButton.onClick { onObjectAdd("queue") }

        val stationButton = VisImageButton(TextureRegionDrawable(stationIcon))
        stationButton.visTextTooltip("Add Station")
        stationButton.onClick { onObjectAdd("station") }

        val resourceButton = VisImageButton(TextureRegionDrawable(resourceIcon))
        resourceButton.visTextTooltip("Add Resource")
        resourceButton.onClick { onObjectAdd("resource") }

        val variableButton = VisImageButton(TextureRegionDrawable(variableIcon))
        variableButton.visTextTooltip("Add Variable")
        variableButton.onClick { onObjectAdd("variable") }

        add(queueButton)
        row()
        add(stationButton)
        row()
        add(resourceButton)
        row()
        add(variableButton)

        pack()
    }

    fun toggle() {
        if (open) fadeOut()
        else fadeIn()

        open = !open
    }

    override fun addCloseButton() {
        val closeButton = VisImageButton("close-window")
        titleTable.add(closeButton).padRight(-padRight + 0.7f)
        closeButton.onClick {
            open = false
            fadeOut()
        }
    }

    override fun fadeOut() {
        touchable = Touchable.disabled;
        addAction(Actions.sequence(Actions.fadeOut(0.3f, Interpolation.fade)))
    }

    override fun fadeIn(): VisWindow {
        touchable = Touchable.enabled;
        addAction(Actions.sequence(Actions.fadeIn(0.3f, Interpolation.fade)))
        return this;
    }
}
