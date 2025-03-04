package ksl.animation.viewer

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisImageButton
import com.kotcrab.vis.ui.widget.VisWindow
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ksl.animation.Assets.assetManager
import ksl.animation.Assets.getUITexture
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.assets.getAsset

class PlaybackWindow(onPlay: () -> Unit, onPause: () -> Unit, onSpeedChange: (tps: Double) -> Unit) : VisWindow("Playback") {
    private var playTexture: TextureRegion = getUITexture(0, 0)
    private var pauseTexture: TextureRegion = getUITexture(1, 0)
    private var playing = false
    private var open = true

    init {
        defaults().pad(20f)
        titleLabel.setAlignment(Align.center)
        x = 10000f
        addCloseButton()

        val group = VerticalGroup().pad(10f)
        val playButton = VisImageButton(TextureRegionDrawable(playTexture))

        val speedModel = SimpleFloatSpinnerModel(1f, 0.1f, 100f, 0.1f)
        val spinner = Spinner("Playback Speed (ticks/s)", speedModel)
        spinner.onChange { onSpeedChange(speedModel.value.toDouble()) }
        group.addActor(playButton)
        group.addActor(spinner)

        add(group)

        pack()

        playButton.onClick {
            playing = !playing
            style.imageUp = if (playing) TextureRegionDrawable(pauseTexture) else TextureRegionDrawable(playTexture)

            if (playing) onPlay()
            else onPause()
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
        return this;
    }
}
