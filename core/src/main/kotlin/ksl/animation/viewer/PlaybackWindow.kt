package ksl.animation.viewer

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.kotcrab.vis.ui.widget.VisImageButton
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisWindow
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ksl.animation.Assets.getUITexture
import ktx.actors.onChange
import ktx.actors.onClick

class PlaybackWindow(
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSpeedChange: (tps: Double) -> Unit,
    onReset: () -> Unit
) : VisWindow("Viewer Controls") {

    private var playTexture: TextureRegion = getUITexture(0, 0)
    private var pauseTexture: TextureRegion = getUITexture(1, 0)
    private var playing = false
    private var open = true

    init {
        defaults().pad(15f)
        titleLabel.setAlignment(Align.center) // Center the title
        x = 10000f
        addCloseButton()

        // Compact rectangular shape
        setSize(360f, 90f)
        setResizable(false)

        val buttonTable = Table()
        buttonTable.defaults().pad(10f).uniform().space(15f)

        // Play/Pause Button
        val playButton = VisImageButton(com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(playTexture))
        playButton.setSize(48f, 48f)
        buttonTable.add(playButton).center()

        // Spinner for playback speed
        val speedModel = SimpleFloatSpinnerModel(1f, 0.1f, 100f, 0.1f)
        val spinner = Spinner("Playback Speed (ticks/s)", speedModel)
        spinner.pad(5f)
        buttonTable.add(spinner).width(140f).center()

        // Reset Button
        val resetButton = VisTextButton("Reset")
        resetButton.pad(10f)
        buttonTable.add(resetButton).width(85f).center()

        // Set layout to window
        add(buttonTable).expand().center()
        pack()
        setPosition(
            (com.badlogic.gdx.Gdx.graphics.width - width) / 2f,
            20f
        )

        // Set interactions
        spinner.onChange { onSpeedChange(speedModel.value.toDouble()) }
        resetButton.onClick { onReset() }
        playButton.onClick {
            playing = !playing
            playButton.style.imageUp = com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                if (playing) pauseTexture else playTexture
            )
            if (playing) onPlay() else onPause()
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
        touchable = Touchable.disabled
        addAction(Actions.sequence(Actions.fadeOut(0.3f, Interpolation.fade)))
    }

    override fun fadeIn(): VisWindow {
        touchable = Touchable.enabled
        addAction(Actions.sequence(Actions.fadeIn(0.3f, Interpolation.fade)))
        return this
    }
}
