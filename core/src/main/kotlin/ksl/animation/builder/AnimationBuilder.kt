package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.kotcrab.vis.ui.VisUI
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.load
import ktx.async.KtxAsync
import ktx.scene2d.Scene2DSkin

object Assets {
    val assetManager = AssetManager()
    private const val TEXTURE_SIZE = 64
    private var uiAtlas: Texture? = null

    init {
        println("Assets Loading...")
        assetManager.finishLoading()
        println("Assets Loaded!")
    }

    fun getUITexture(x: Int, y: Int, width: Int = 1, height: Int = 1): TextureRegion {
        if (uiAtlas == null) uiAtlas = assetManager.get("ui_atlas.png", Texture::class.java)
        return TextureRegion(uiAtlas, x * TEXTURE_SIZE, y * TEXTURE_SIZE, width * TEXTURE_SIZE, height * TEXTURE_SIZE)
    }
}

class Main : KtxGame<KtxScreen>() {

    override fun create() {
        KtxAsync.initiate()

        VisUI.load()
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        Gdx.graphics.setWindowedMode(1080, 720)

        // Load assets before setting the screen
        Assets.assetManager.finishLoading()

        // Add the builder screen
        val builderScreen = AnimationBuilderScreen()
        addScreen(builderScreen)

        // Set the starting screen
        setScreen<AnimationBuilderScreen>()
    }

    override fun dispose() {
        VisUI.dispose()
        Assets.assetManager.dispose()
        super.dispose()
    }
}
