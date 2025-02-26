package ksl.animation

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kotcrab.vis.ui.VisUI
import ksl.animation.Assets.assetManager
import ksl.animation.viewer.AnimationViewerScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

object Assets {
    val assetManager = AssetManager()
    private const val TEXTURE_SIZE = 64
    private var uiAtlas: Texture? = null

    init {
        println("Assets Loading...")
        assetManager.load<Texture>("ui_atlas.png")
        assetManager.finishLoading();
        println("Assets Loaded!")
    }

    fun getUITexture(x: Int, y: Int, width: Int = 1, height: Int = 1): TextureRegion {
        if (uiAtlas == null) uiAtlas = assetManager.getAsset<Texture>("ui_atlas.png")
        return TextureRegion(uiAtlas, x * TEXTURE_SIZE, y * TEXTURE_SIZE, width * TEXTURE_SIZE, height * TEXTURE_SIZE)
    }
}

class Main : KtxGame<KtxScreen>() {

    override fun create() {
        KtxAsync.initiate()

        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        Gdx.graphics.setWindowedMode(1080, 720)

        // all assets loaded
        if (assetManager.update()) {
            addScreen(AnimationViewerScreen())
            setScreen<AnimationViewerScreen>()
        }
    }

    override fun dispose() {
        VisUI.dispose()
        assetManager.dispose()
        super.dispose()
    }
}
