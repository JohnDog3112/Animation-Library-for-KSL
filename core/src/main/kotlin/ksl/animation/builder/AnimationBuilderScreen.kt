package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.MenuBar
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.file.FileChooser
import ksl.animation.viewer.AnimationViewerScreen
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.scene2d.vis.menu
import ktx.scene2d.vis.menuItem

class AnimationBuilderScreen(private val game: KtxGame<KtxScreen>) : KtxScreen, InputAdapter() {
    private val stage = Stage(ScreenViewport())
    private val objectSelector = ObjectSelectorWindow()
    private val fileChooser = FileChooser(FileChooser.Mode.OPEN)
    private var animationBuilder = AnimationBuilder()

    override fun show() {
        val input = InputMultiplexer()
        input.addProcessor(stage)
        input.addProcessor(this)
        Gdx.input.inputProcessor = input

        val root = VisTable()
        root.setFillParent(true)

        val menuBar = MenuBar()
        root.add(menuBar.table).expandX().fillX().row()

        val modeTable = VisTable()
        modeTable.background = VisUI.getSkin().getDrawable("separator-menu")
        modeTable.add(VisLabel("Builder Mode"))
        root.add(modeTable).fillX().row()
        root.add().expand().fill()

        val fileMenu = menuBar.menu("File")
        val importItem = fileMenu.menuItem("Import...")
        importItem.onClick {
            this@AnimationBuilderScreen.stage.addActor(fileChooser.fadeIn())
        }

        val switchToViewerItem = fileMenu.menuItem("Switch to Viewer...")
        switchToViewerItem.onClick {
            this@AnimationBuilderScreen.game.setScreen<AnimationViewerScreen>()
        }

        val viewMenu = menuBar.menu("View")

        val showGridLinesItem = viewMenu.menuItem("Toggle Grid Lines...")
        showGridLinesItem.onClick {
            this@AnimationBuilderScreen.animationBuilder.showGridLines = !this@AnimationBuilderScreen.animationBuilder.showGridLines
        }

        stage.addActor(root)
        stage.addActor(objectSelector)
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        animationBuilder.render(delta)

        stage.act(delta)
        stage.draw()
    }
}
