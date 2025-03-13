package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.Menu
import com.kotcrab.vis.ui.widget.MenuItem
import com.kotcrab.vis.ui.widget.MenuBar
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.kotcrab.vis.ui.widget.file.FileChooser


class AnimationBuilderScreen : KtxScreen {
    private val stage = Stage(ScreenViewport())
    private val undoStack = mutableListOf<Runnable>()
    private val redoStack = mutableListOf<Runnable>()


    init {
        Gdx.input.inputProcessor = stage

        // Create the root table
        val root = VisTable()
        root.setFillParent(true)

        // Create the menu bar
        val menuBar = createMenuBar()
        val menuTable = Table()
        menuTable.add(menuBar.table).expandX().fillX().top()

        root.add(menuTable).expandX().fillX().top().row()

        // Middle layout
        val middle = VisTable()
        val mainArea = createPanel("Main Working Area", 600f, 300f, Color.LIME)

        // Left panel
        val leftPanel = VisTable()
        leftPanel.add(createPanel("Properties", 150f, 200f)).pad(10f).row()
        leftPanel.add(createPanel("Draggable objects", 150f, 200f)).pad(10f).row()


        val tools = VisTable()
        for (i in 0..1) {
            for (j in 0..2) {
                val button = VisTextButton("")

                // Make the button draggable
                button.addListener(object : InputListener() {
                    var offsetX = 0f
                    var offsetY = 0f

                    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        offsetX = x
                        offsetY = y
                        event?.listenerActor?.let {
                            it.parent?.removeActor(it) // Remove from its current position
                            stage.addActor(it) // Add back to the stage (on top)
                        }
                        return true // Capture the touch
                    }

                    override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                        val actor = event?.listenerActor ?: return
                        actor.moveBy(x - offsetX, y - offsetY)
                    }
                })

                tools.add(button).size(40f).pad(5f)
            }
            tools.row()
        }
        leftPanel.add(tools).pad(10f)

        // Right panel
        val rightPanel = VisTable()
        rightPanel.add(createPanel("Path editor", 150f, 300f)).pad(10f)



        middle.add(leftPanel).top().pad(10f)
        middle.add(mainArea).expand().fill().pad(10f)
        middle.add(rightPanel).top().pad(10f)
        middle.row()

        // Bottom layout
        val bottom = VisTable()
        bottom.add(createPanel("Object Editor", 400f, 150f)).pad(10f)
        bottom.add(createPanel("Layers", 200f, 150f)).pad(10f)

        root.add(middle).expand().fill().row()
        root.add(bottom).expandX().fillX().pad(10f)

        stage.addActor(root)
    }


    private fun performAction(action: Runnable) {
        undoStack.add(action)
        redoStack.clear() // Clear redoStack because a new action invalidates the redo history
        action.run() // Execute the action immediately
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastAction = undoStack.removeLast()
            redoStack.add(lastAction)
            println("Undo performed")

        } else {
            println("Nothing to Undo")
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val lastRedo = redoStack.removeLast()
            undoStack.add(lastRedo)
            lastRedo.run()
            println("Redo performed")
        } else {
            println("Nothing to Redo")
        }
    }



    private fun createMenuBar(): MenuBar {
        val menuBar = MenuBar()


        // File Menu
        val fileMenu = Menu("File")

        val newItem = MenuItem("New").apply {
            addListener (object: ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    println("new file created!")
                }
            })
        }

        val openItem = MenuItem("Open").apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    val fileChooser = FileChooser(FileChooser.Mode.OPEN)
                    stage.addActor(fileChooser)
                }
            })
        }

        val saveItem = MenuItem("Save").apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    val fileChooser = FileChooser(FileChooser.Mode.SAVE) // Save file dialog
                    stage.addActor(fileChooser) // Add to the UI
                }
            })
        }

        val exitItem = MenuItem("Exit").apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    Gdx.app.exit() // Closes the app when clicked
                }
            })
        }

        fileMenu.addItem(newItem)
        fileMenu.addItem(openItem)
        fileMenu.addItem(saveItem)
        fileMenu.addItem(exitItem)

        // Edit Menu
        val editMenu = Menu("Edit")
        val undoItem = MenuItem("Undo").apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    undo()
                }
            })
        }

        val redoItem = MenuItem("Redo").apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: com.badlogic.gdx.scenes.scene2d.Actor) {
                    redo()
                }
            })
        }

        editMenu.addItem(undoItem)
        editMenu.addItem(redoItem)

        // View Menu NEED TO ADD STUFF HERE IF APPLICABLE
        val viewMenu = Menu("View")
        val zoomInItem = MenuItem("Zoom In").apply { addListener { println("Zooming In"); true } }
        val zoomOutItem = MenuItem("Zoom Out").apply { addListener { println("Zooming Out"); true } }
        viewMenu.addItem(zoomInItem)
        viewMenu.addItem(zoomOutItem)

        // Insert Menu NEED TO ADD STUFF HERE
        val insertMenu = Menu("Insert")
        val addObjectItem = MenuItem("Add Object").apply { addListener { println("New Object Inserted"); true } }
        val addPathItem = MenuItem("Add Path").apply { addListener { println("Path Added"); true } }
        insertMenu.addItem(addObjectItem)
        insertMenu.addItem(addPathItem)

//        // Export Menu
//        val exportMenu = Menu("Export")
//        val exportPngItem = MenuItem("Export as PNG").apply { addListener { println("Exporting as PNG"); true } }
//        val exportJsonItem = MenuItem("Export as JSON").apply { addListener { println("Exporting as JSON"); true } }
//        exportMenu.addItem(exportPngItem)
//        exportMenu.addItem(exportJsonItem)

//        // Format Menu
//        val formatMenu = Menu("Format")
//        val alignLeftItem = MenuItem("Align Left").apply { addListener { println("Aligned Left"); true } }
//        val alignRightItem = MenuItem("Align Right").apply { addListener { println("Aligned Right"); true } }
//        formatMenu.addItem(alignLeftItem)
//        formatMenu.addItem(alignRightItem)

        // Builder Menu
        val builderMenu = Menu("Builder")
        val buildSceneItem = MenuItem("Build Scene").apply { addListener { println("Scene Built"); true } }
        builderMenu.addItem(buildSceneItem)

        // Viewer Menu
        val viewerMenu = Menu("Viewer")
        val previewItem = MenuItem("Preview").apply { addListener { println("Previewing Scene"); true } }
        viewerMenu.addItem(previewItem)

        // Add all menus to the menu bar
        menuBar.addMenu(fileMenu)
        menuBar.addMenu(editMenu)
        menuBar.addMenu(viewMenu)
        menuBar.addMenu(insertMenu)
//        menuBar.addMenu(exportMenu)
//        menuBar.addMenu(formatMenu)
        menuBar.addMenu(builderMenu)
        menuBar.addMenu(viewerMenu)

        return menuBar
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

    private fun createDrawable(color: Color): TextureRegionDrawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        val drawable = TextureRegionDrawable(Texture(pixmap))
        pixmap.dispose()
        return drawable
    }
}
