package ksl.animation

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.kotcrab.vis.ui.VisUI
import ksl.animation.Assets.assetManager
import ksl.animation.builder.AnimationBuilderScreen
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
    companion object {
        const val STARTING_WIDTH = 1080
        const val STARTING_HEIGHT = 720
        val camera: OrthographicCamera = OrthographicCamera()
        lateinit var defaultFont: BitmapFont
    }

    override fun create() {
        KtxAsync.initiate()

        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()
        defaultFont = BitmapFont(true)

        camera.setToOrtho(true, STARTING_WIDTH.toFloat(), STARTING_HEIGHT.toFloat())
        Gdx.graphics.setWindowedMode(STARTING_WIDTH, STARTING_HEIGHT)

        // all assets loaded
        if (assetManager.update()) {
            addScreen(AnimationViewerScreen(this))
            addScreen(AnimationBuilderScreen(this))
            setScreen<AnimationBuilderScreen>()
        }
    }

    override fun resize(width: Int, height: Int) {
        camera.setToOrtho(true, width.toFloat(), height.toFloat())
        super.resize(width, height)
    }

    override fun dispose() {
        VisUI.dispose()
        assetManager.dispose()
        super.dispose()
    }
}
