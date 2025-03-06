package ksl.animation.viewer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.FocusManager
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.MenuBar
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.FileChooser.DefaultFileIconProvider
import com.kotcrab.vis.ui.widget.file.FileTypeFilter
import com.kotcrab.vis.ui.widget.file.StreamingFileChooserListener
import ksl.animation.builder.AnimationBuilderScreen
import ksl.animation.setup.KSLAnimation
import ksl.animation.sim.KSLAnimationLog
import ksl.animation.util.parseJsonToAnimation
import ktx.actors.onClick
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.scene2d.vis.menu
import ktx.scene2d.vis.menuItem
import java.io.File
import java.util.zip.ZipFile

class AnimationViewerScreen(private val game: KtxGame<KtxScreen>) : KtxScreen, InputAdapter() {
    private val stage: Stage = Stage(ScreenViewport())
    private val playbackWindow: PlaybackWindow = PlaybackWindow({ animationViewer.playing = true }, { animationViewer.playing = false }, { tps -> animationViewer.ticksPerSecond = tps })
    private val fileChooser: FileChooser = FileChooser(FileChooser.Mode.OPEN)
    private var animationViewer: AnimationViewer = AnimationViewer()
    private lateinit var animation: KSLAnimation
    private lateinit var animationLog: KSLAnimationLog
    private var animationLoaded = false
    private var controlPressed = false

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
        animationViewer = AnimationViewer()

        animationViewer.loadAnimationSetup(parseJsonToAnimation(setupFile))
        animationLog = KSLAnimationLog(simFile, animationViewer)

        animationLoaded = true
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
        modeTable.add(VisLabel("Viewer Mode"))
        root.add(modeTable).fillX().row()
        root.add().expand().fill()

        val fileMenu = menuBar.menu("File")
        val importItem = fileMenu.menuItem("Import...")
        importItem.onClick {
            this@AnimationViewerScreen.stage.addActor(fileChooser.fadeIn())
        }

        val switchToBuilderItem = fileMenu.menuItem("Switch to Builder...")
        switchToBuilderItem.onClick {
            this@AnimationViewerScreen.game.setScreen<AnimationBuilderScreen>()
        }

        val viewMenu = menuBar.menu("View")
        val playbackWindowItem = viewMenu.menuItem("Toggle Playback Window...")
        playbackWindowItem.setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.P)
        playbackWindowItem.onClick {
            this@AnimationViewerScreen.playbackWindow.toggle()
        }

        val showGridLinesItem = viewMenu.menuItem("Toggle Grid Lines...")
        showGridLinesItem.onClick {
            this@AnimationViewerScreen.animationViewer.showGridLines = !this@AnimationViewerScreen.animationViewer.showGridLines
        }

        val showStationsItem = viewMenu.menuItem("Toggle Station Rendering...")
        showStationsItem.onClick {
            this@AnimationViewerScreen.animationViewer.showStations = !this@AnimationViewerScreen.animationViewer.showStations
        }

        val showIdsItem = viewMenu.menuItem("Toggle ID Rendering...")
        showIdsItem.onClick {
            this@AnimationViewerScreen.animationViewer.showIds = !this@AnimationViewerScreen.animationViewer.showIds
        }

        stage.addActor(root)
        stage.addActor(playbackWindow)

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
                loadAnimation(file.path())
            }
        })

        FocusManager.resetFocus(stage)
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)
        stage.act(delta)
        stage.draw()

        if (animationLoaded) {
            animationViewer.render(delta)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.CONTROL_LEFT) controlPressed = true
        if (!controlPressed) return false

        if (keycode == Input.Keys.P) {
            playbackWindow.toggle()
            return true
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.ESCAPE) Gdx.app.exit()

        if (keycode == Input.Keys.CONTROL_LEFT) controlPressed = false

        return false
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        animationViewer.dispose()
        super.dispose()
    }
}
