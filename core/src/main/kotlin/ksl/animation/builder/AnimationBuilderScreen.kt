package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class AnimationBuilderScreen : KtxScreen {
    private val stage = Stage(ScreenViewport())

    init {
        Gdx.input.inputProcessor = stage

        // Main layout
        val root = VisTable()
        root.setFillParent(true)

        // Top menu bar - make it look a lot better
        val topMenu = VisTable()
        topMenu.add(createButton("File")).pad(5f)
        topMenu.add(createButton("Edit")).pad(5f)
        topMenu.add(createButton("View")).pad(5f)
        topMenu.add(createButton("Insert")).pad(5f)
        topMenu.add(createButton("Export")).pad(5f)
        topMenu.add(createButton("Format")).pad(5f)
        topMenu.add(createButton("Builder")).pad(5f)
        topMenu.add(createButton("Viewer")).pad(5f)
        root.add(topMenu).expandX().fillX().top().row()

        // Middle layout
        val middle = VisTable()

        // Left panel - add a drop down for properties or put up top????
        val leftPanel = VisTable()
        leftPanel.add(createPanel("Properties", 150f, 200f)).pad(10f).row()
        leftPanel.add(createPanel("Draggable objects", 150f, 200f)).pad(10f).row()

        // Common tools - add more later
        val tools = VisTable()
        for (i in 0..1) {
            for (j in 0..2) {
                tools.add(createButton("")).size(40f).pad(5f)
            }
            tools.row()
        }
        leftPanel.add(tools).pad(10f)

        // Main working area
        val mainArea = createPanel("Main Working Area", 600f, 300f, Color.LIME)

        // Right panel - didnt really know what to put here
        val rightPanel = VisTable()
        rightPanel.add(createPanel("Path editor", 150f, 300f)).pad(10f)

        middle.add(leftPanel).top().pad(10f)
        middle.add(mainArea).expand().fill().pad(10f)
        middle.add(rightPanel).top().pad(10f)
        middle.row()

        // Bottom layout - could add more down here? idk
        val bottom = VisTable()
        bottom.add(createPanel("Object Editor", 400f, 150f)).pad(10f)
        bottom.add(createPanel("Layers", 200f, 150f)).pad(10f)

        root.add(middle).expand().fill().row()
        root.add(bottom).expandX().fillX().pad(10f)

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        clearScreen(0.9f, 0.9f, 0.9f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.disposeSafely()
    }

    private fun createPanel(title: String, width: Float, height: Float, color: Color = Color.LIGHT_GRAY): VisTable {
        val table = VisTable()
        table.background = createDrawable(color)
        table.add(VisLabel(title)).pad(5f).center()
        table.setSize(width, height)
        return table
    }

    private fun createButton(text: String): TextButton {
        return VisTextButton(text)
    }

    private fun createDrawable(color: Color): TextureRegionDrawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        val drawable = TextureRegionDrawable(Texture(pixmap))
        pixmap.dispose()
        return drawable
    }
}
