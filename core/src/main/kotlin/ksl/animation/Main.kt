package ksl.animation

import com.badlogic.gdx.Gdx
import com.kotcrab.vis.ui.VisUI
import ksl.animation.viewer.AnimationViewerScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

class Main : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()
        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        Gdx.graphics.setWindowedMode(1080, 720)

        addScreen(AnimationViewerScreen())
        setScreen<AnimationViewerScreen>()
    }

    override fun dispose() {
        VisUI.dispose();
        super.dispose()
    }
}
