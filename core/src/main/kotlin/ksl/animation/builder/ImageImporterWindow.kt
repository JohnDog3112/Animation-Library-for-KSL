package ksl.animation.builder

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.FileChooser.DefaultFileIconProvider
import com.kotcrab.vis.ui.widget.file.FileTypeFilter
import com.kotcrab.vis.ui.widget.file.StreamingFileChooserListener
import ktx.actors.onClick

class ImageAdapter(array: ArrayList<String>) : ArrayListAdapter<String, VisTable>(array) {
    private val bg: Drawable = VisUI.getSkin().getDrawable("window-bg")
    private val selection: Drawable = VisUI.getSkin().getDrawable("list-selection")

    init {
        selectionMode = SelectionMode.SINGLE;
    }

    override fun createView(imageName: String?): VisTable {
        val table = VisTable()
        table.add(VisLabel(imageName!!))
        return table
    }

    override fun selectView(view: VisTable?) {
        view!!.background = selection
    }

    override fun deselectView(view: VisTable?) {
        view!!.background = bg
    }
}

class ImageImporterWindow(private val builder: AnimationBuilder) : VisWindow("Image Importer") {
    private var open = false
    private val fileChooser = FileChooser(FileChooser.Mode.OPEN)
    private val imageAdapter = ImageAdapter(ArrayList(builder.images.keys.toList()))
    private val imageView = ListView(imageAdapter)
    private val imageIdField = VisTextField()
    private val selectedImage = VisImage()
    private val updateIdButton = VisTextButton("Update")
    private val deleteImageButton = VisTextButton("Delete")
    private var imageCount = 0
    private var currentImageId = ""

    init {
        titleLabel.setAlignment(Align.center)
        addCloseButton()
        centerWindow()

        isModal = true
        isMovable = false

        // setup file chooser
        fileChooser.selectionMode = FileChooser.SelectionMode.FILES
        fileChooser.isFavoriteFolderButtonVisible = true
        fileChooser.isShowSelectionCheckboxes = true
        fileChooser.iconProvider = DefaultFileIconProvider(fileChooser)

        val typeFilter = FileTypeFilter(true)
        typeFilter.addRule("Image file (*.jpg, *.png)", "jpg", "png")
        fileChooser.setFileTypeFilter(typeFilter)

        fileChooser.setListener(object : StreamingFileChooserListener() {
            override fun selected(file: FileHandle) {
                val pixmap = Pixmap(file)
                val imageId = "new_image_$imageCount"
                builder.images[imageId] = Pair(pixmap, Texture(pixmap))
                imageCount++

                loadImage(imageId)
                updateImages()
            }
        })

        defaults().pad(20f)

        val leftTable = VisTable()
        val imageIdRow = VisTable()

        imageIdRow.defaults().pad(0f, 0f, 10f, 0f)
        imageIdRow.add(VisLabel("Image ID: "))
        imageIdRow.add(imageIdField)

        val addImageButton = VisTextButton("Add")
        addImageButton.onClick {
            stage.addActor(fileChooser.fadeIn())
        }

        updateIdButton.onClick {
            builder.images[imageIdField.text] = builder.images[currentImageId]!!
            builder.images.remove(currentImageId)
            loadImage(imageIdField.text)
            updateImages()
        }

        deleteImageButton.onClick {
            builder.images.remove(currentImageId)
            loadImage("DEFAULT")
            updateImages()
        }

        val buttonTable = VisTable()
        buttonTable.defaults().pad(10f, 20f, 0f, 20f)
        buttonTable.add(updateIdButton)
        buttonTable.add(deleteImageButton)

        leftTable.add(imageIdRow).row()
        leftTable.add(selectedImage).width(200f).height(200f).row()
        leftTable.add(buttonTable).row()
        add(leftTable).fillY().left()

        imageAdapter.setItemClickListener { loadImage(it) }

        val rightTable = VisTable()
        rightTable.add(imageView.mainTable).grow().row()
        rightTable.add(addImageButton)
        add(rightTable).fillY().right()
        fadeOut(true)
        loadImage("DEFAULT")

        pack()
    }

    fun toggle() {
        if (open) fadeOut()
        else fadeIn()

        open = !open
        centerWindow()
    }

    private fun loadImage(imageId: String) {
        imageIdField.text = imageId
        selectedImage.setDrawable(builder.images[imageId]?.second)

        imageIdField.isDisabled = (imageId == "DEFAULT")
        updateIdButton.isDisabled = (imageId == "DEFAULT")
        deleteImageButton.isDisabled = (imageId == "DEFAULT")

        currentImageId = imageId
    }

    private fun updateImages() {
        imageAdapter.clear()
        imageAdapter.addAll(ArrayList(builder.images.keys.toList()))
        pack()
    }

    override fun addCloseButton() {
        val closeButton = VisImageButton("close-window")
        titleTable.add(closeButton).padRight(-padRight + 0.7f)
        closeButton.onClick {
            open = false
            fadeOut()
        }
    }

    private fun fadeOut(instant: Boolean) {
        touchable = Touchable.disabled
        addAction(Actions.fadeOut(if (instant) 0f else 0.3f, Interpolation.fade))
    }

    override fun fadeOut() {
        fadeOut(false)
    }

    override fun fadeIn(): VisWindow {
        touchable = Touchable.enabled
        addAction(Actions.fadeIn(0.3f, Interpolation.fade))
        return this
    }
}
