package ksl.animation.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.MenuBar
import com.kotcrab.vis.ui.widget.VisDialog
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.FileChooser.DefaultFileIconProvider
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter
import com.kotcrab.vis.ui.widget.file.FileTypeFilter
import com.kotcrab.vis.ui.widget.file.StreamingFileChooserListener
import ksl.animation.util.parseAnimationToJson
import ksl.animation.common.renderables.*
import ksl.animation.sim.KSLAnimationLog
import ksl.animation.util.parseJsonToAnimation
import ksl.animation.viewer.AnimationViewerScreen
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.scene2d.vis.menu
import ktx.scene2d.vis.menuItem
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class AnimationBuilderScreen(private val game: KtxGame<KtxScreen>) : KtxScreen, InputAdapter() {
    companion object {
        lateinit var imageImporterWindow: ImageImporterWindow
        lateinit var objectTypeEditorWindow: ObjectTypeEditorWindow
        data class SaveInfo(val pathStr: String, var simFileStr: String)
    }

    private val stage = Stage(ScreenViewport())
    private val addObjectWindow = AddObjectWindow({ type -> animationBuilder.addObject(type) })
    private val objectEditor = ObjectEditorWindow(this)
    private val fileChooser = FileChooser(FileChooser.Mode.OPEN)
    private val loadingDialog = VisDialog("Loading...")
    private val saveFileChooser = FileChooser(FileChooser.Mode.SAVE)
    var animationBuilder = AnimationBuilder({ renderable -> objectEditor.showObject(renderable) })

    private var controlPressed = false

    fun removeObject(kslObject: KSLRenderable) {
        animationBuilder.removeObject(kslObject)
        objectEditor.showObject(null)
    }

    fun copyObject(kslObject: KSLRenderable) {
        animationBuilder.copyObject(kslObject)
    }

    fun loadAnimation(zipFileName: String) {
        try {
            ZipFile(File(zipFileName)).use { zip ->
                val setupFile = zip.getEntry("setup.json")
                val simFile = zip.getEntry("sim.log")

                if (setupFile != null && simFile != null) {
                    zip.getInputStream(setupFile).bufferedReader().use { setupReader ->
                        zip.getInputStream(simFile).bufferedReader().use { simReader ->
                            val setupJson = setupReader.readText()
                            val simJson = simReader.readText()
                            loadAnimation(setupJson, simJson)
                        }
                    }
                } else {
                    println("setup.json or sim.json not found in zip file.")
                }
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
            println("Invalid file!")
        }
    }

    private fun loadAnimation(setupFile: String, simFile: String) {
        loadingDialog.show(stage)
        animationBuilder.loadAnimationSetup(parseJsonToAnimation(setupFile))
        loadingDialog.hide()
        saveInfo?.simFileStr = simFile
    }

    private var saveInfo: SaveInfo? = null

    private fun resetBuilder() {
        this.animationBuilder.resetScene()
        this.saveInfo = null
    }

    private fun saveAnimation(saveInfo: SaveInfo) {
        val serializedJson = animationBuilder.serialize()

        try {
            val zipOut = ZipOutputStream(FileOutputStream(saveInfo.pathStr))

            val setupJson = ZipEntry("setup.json")
            zipOut.putNextEntry(setupJson)
            val setupJsonData = parseAnimationToJson(serializedJson).encodeToByteArray()
            zipOut.write(setupJsonData)
            zipOut.closeEntry()

            val simFile = ZipEntry("sim.log")
            zipOut.putNextEntry(simFile)
            val simFileData = saveInfo.simFileStr.encodeToByteArray()
            zipOut.write(simFileData)
            zipOut.closeEntry()

            println(simFileData)

            zipOut.close()
        } catch (e: Exception) {
            println(e.printStackTrace())
            println("Invalid file!")
        }
    }

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

        val windowTable = VisTable()
        windowTable.add(addObjectWindow)
        windowTable.add().expandX()
        windowTable.add(objectEditor)

        root.add(windowTable).expand().fill().row()

        val fileMenu = menuBar.menu("File")
        val newItem = fileMenu.menuItem("New File...")
        newItem.onClick {
            resetBuilder()
        }
        val importItem = fileMenu.menuItem("Import...")
        importItem.onClick {
            this@AnimationBuilderScreen.stage.addActor(fileChooser.fadeIn())
        }

        // setup file chooser
        fileChooser.selectionMode = FileChooser.SelectionMode.FILES
        fileChooser.isFavoriteFolderButtonVisible = true
        fileChooser.isShowSelectionCheckboxes = true
        fileChooser.iconProvider = DefaultFileIconProvider(fileChooser)

        val typeFilter = FileTypeFilter(true)
        typeFilter.addRule("Animation file (*.anim, *.zip)", "anim", "zip")
        fileChooser.setFileTypeFilter(typeFilter)

        fileChooser.setListener(object : StreamingFileChooserListener() {
            override fun selected(file: FileHandle) {
                val tmpInfo = SaveInfo(file.path(), "")
                saveInfo = tmpInfo
                loadAnimation(file.path())
            }

            override fun canceled() {
                fileChooser.fadeOut()
            }
        })

        val saveItem = fileMenu.menuItem("Save...")
        saveItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.S)
        saveItem.onClick {
            val tmpInfo = saveInfo
            if (tmpInfo == null) {
                this@AnimationBuilderScreen.stage.addActor(saveFileChooser.fadeIn())
            } else {
                saveAnimation(tmpInfo)
            }
        }

        // setup file chooser
        saveFileChooser.selectionMode = FileChooser.SelectionMode.FILES
        saveFileChooser.isFavoriteFolderButtonVisible = true
        saveFileChooser.isShowSelectionCheckboxes = true
        saveFileChooser.iconProvider = DefaultFileIconProvider(fileChooser)

        val saveTypeFilter = FileTypeFilter(true)
        saveTypeFilter.addRule("Animation file (*.anim, *.zip)", "anim", "zip")
        saveFileChooser.setFileTypeFilter(saveTypeFilter)

        saveFileChooser.setListener(object : StreamingFileChooserListener() {
            override fun selected(file: FileHandle) {
                val tmpInfo = SaveInfo(file.path(), "")
                saveInfo = tmpInfo
                saveAnimation(tmpInfo)
            }

            override fun canceled() {
                saveFileChooser.fadeOut()
            }
        })

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
            animationBuilder.showGridLines = !animationBuilder.showGridLines
        }

        val showIdsItem = viewMenu.menuItem("Toggle ID Rendering...")
        showIdsItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.I)
        showIdsItem.onClick {
            animationBuilder.showIds = !animationBuilder.showIds
        }

        val windowMenu = menuBar.menu("Window")

        val showAddObjectWindow = windowMenu.menuItem("Toggle Add Object Window...")
        showAddObjectWindow.onClick {
            addObjectWindow.toggle()
        }

        val showObjectEditorWindow = windowMenu.menuItem("Toggle Object Editor Window...")
        showObjectEditorWindow.onClick {
            objectEditor.toggle()
        }

        val showImageImporterWindow = windowMenu.menuItem("Toggle Image Importer Window...")
        showImageImporterWindow.onClick {
            imageImporterWindow.toggle()
        }

        val showObjectTypeEditorWindow = windowMenu.menuItem("Toggle Object Type Editor Window...")
        showObjectTypeEditorWindow.onClick {
            objectTypeEditorWindow.toggle()
        }

        imageImporterWindow = ImageImporterWindow(animationBuilder)
        objectTypeEditorWindow = ObjectTypeEditorWindow(animationBuilder)
        stage.addActor(imageImporterWindow)
        stage.addActor(objectTypeEditorWindow)
        stage.addActor(root)
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

        if (keycode == Input.Keys.S) {
            val tmpInfo = saveInfo
            if (tmpInfo == null) {
                this@AnimationBuilderScreen.stage.addActor(saveFileChooser.fadeIn())
            } else {
                saveAnimation(tmpInfo)
            }
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
