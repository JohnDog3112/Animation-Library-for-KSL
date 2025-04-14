package ksl.animation.builder

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.*
import ksl.animation.common.renderables.KSLRenderable
import ktx.actors.onClick

class ObjectEditorWindow(private val builderScreen: AnimationBuilderScreen) : VisWindow("Object Editor") {
    private var open = true
    private val content = VisTable()

    init {
        titleLabel.setAlignment(Align.center)
        addCloseButton()

        defaults().pad(20f, 40f, 20f, 40f)

        isMovable = false
        isKeepWithinParent = false
        setKeepWithinStage(false)

        add(content).grow()

        showObject(null)
    }

    fun showObject(kslObject: KSLRenderable?, stage: Stage) {
        content.clear()

        content.defaults().center()
        if (kslObject != null) {
            kslObject.openEditor(builderScreen.animationBuilder, content)

            val editButtons = VisTable()
            editButtons.defaults().pad(30f, 10f, 0f, 10f)

            val deleteButton = VisTextButton("Delete")
            deleteButton.onClick { builderScreen.removeObject(kslObject) }

            val copyButton = VisTextButton("Copy")
            copyButton.onClick { builderScreen.copyObject(kslObject) }

            editButtons.add(deleteButton)
            editButtons.add(copyButton)
            content.add(editButtons)
        } else {
            content.add(VisLabel("Nothing Selected"))
        }
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
        return this
    }
}
