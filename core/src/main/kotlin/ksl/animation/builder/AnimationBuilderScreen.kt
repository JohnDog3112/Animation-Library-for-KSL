package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
    private val objectSelector = ObjectSelectorWindow({ type -> animationBuilder.addObject(type) })
    private val fileChooser = FileChooser(FileChooser.Mode.OPEN)
    private var animationBuilder = AnimationBuilder()
    private var controlPressed = false

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

        val editMenu = menuBar.menu("Edit")
        val undoItem = editMenu.menuItem("Undo...")
        undoItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.Z)
        undoItem.onClick {
            animationBuilder.undo()
        }

        val redoItem = editMenu.menuItem("Redo...")
        redoItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.Y)
        redoItem.onClick {
            animationBuilder.redo()
        }

        val snapToGridItem = editMenu.menuItem("Toggle Snap to Grid...")
        snapToGridItem.onClick {
            animationBuilder.snapToGrid = !animationBuilder.snapToGrid
        }

        val switchToViewerItem = fileMenu.menuItem("Switch to Viewer...")
        switchToViewerItem.onClick {
            this@AnimationBuilderScreen.game.setScreen<AnimationViewerScreen>()
        }

        val viewMenu = menuBar.menu("View")

        val showGridLinesItem = viewMenu.menuItem("Toggle Grid Lines...")
        showGridLinesItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.G)
        showGridLinesItem.onClick {
            this@AnimationBuilderScreen.animationBuilder.showGridLines = !this@AnimationBuilderScreen.animationBuilder.showGridLines
        }

        val showIdsItem = viewMenu.menuItem("Toggle ID Rendering...")
        showIdsItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.I)
        showIdsItem.onClick {
            this@AnimationBuilderScreen.animationBuilder.showIds = !this@AnimationBuilderScreen.animationBuilder.showIds
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

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.CONTROL_LEFT) controlPressed = true
        if (!controlPressed) return false

        if (keycode == Input.Keys.G) {
            animationBuilder.showGridLines = !animationBuilder.showGridLines
            return true
        }

        if (keycode == Input.Keys.I) {
            animationBuilder.showIds = !animationBuilder.showIds
            return true
        }

        if (keycode == Input.Keys.Z) {
            animationBuilder.selectedObject = ""
            animationBuilder.undo()
            return true
        }

        if (keycode == Input.Keys.Y) {
            animationBuilder.selectedObject = ""
            animationBuilder.redo()
            return true
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE) Gdx.app.exit()

        if (keycode == Input.Keys.CONTROL_LEFT) controlPressed = false

        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        animationBuilder.onMouseMove(screenX, screenY)
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        animationBuilder.onMouseMove(screenX, screenY)
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        animationBuilder.onMouseDown(screenX, screenY, button)
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        animationBuilder.onMouseUp(screenX, screenY, button)
        return false
    }

    override fun resize(width: Int, height: Int) {
        animationBuilder.resize()
        stage.viewport.update(width, height, true)
        super.resize(width, height)
    }
}
