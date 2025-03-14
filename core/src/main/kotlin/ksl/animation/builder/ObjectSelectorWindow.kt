package ksl.animation.builder

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisImageButton
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisWindow
import ktx.actors.onClick

class ObjectSelectorWindow(onObjectAdd: (type: String) -> Unit) : VisWindow("Object Selector") {
    private var open = true

    init {
        defaults().pad(20f)
        titleLabel.setAlignment(Align.center)
        addCloseButton()

        val queueButton = VisTextButton("Queue")
        queueButton.onClick { onObjectAdd("queue") }
        val stationButton = VisTextButton("Station")
        stationButton.onClick { onObjectAdd("station") }
        val resourceButton = VisTextButton("Resource")
        resourceButton.onClick { onObjectAdd("resource") }
        add(queueButton)
        add(stationButton)
        add(resourceButton)

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
