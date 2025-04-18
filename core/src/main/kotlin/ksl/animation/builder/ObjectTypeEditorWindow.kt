package ksl.animation.builder

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.adapter.ArrayListAdapter
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.FileChooser.DefaultFileIconProvider
import com.kotcrab.vis.ui.widget.file.FileTypeFilter
import com.kotcrab.vis.ui.widget.file.StreamingFileChooserListener
import ksl.animation.setup.KSLAnimationObject
import ktx.actors.onChange
import ktx.actors.onClick

class ObjectTypeAdapter(array: ArrayList<String>) : ArrayListAdapter<String, VisTable>(array) {
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

class ObjectTypeEditorWindow(private val builder: AnimationBuilder) : VisWindow("Object Type Editor Window") {
    private var open = false
    private val objectTypeAdapter = ObjectTypeAdapter(ArrayList(builder.objectTypes.keys.toList()))
    private val objectTypeView = ListView(objectTypeAdapter)
    private val objectIdTextField = VisTextField()
    private val selectedObjectType = VisImage()
    private val updateIdButton = VisTextButton("Update")
    private val deleteObjectTypeButton = VisTextButton("Delete")
    private val imageDropDown = VisSelectBox<String>()
    private val addImageButton = VisTextButton("+")
    private var objectTypeCount = 0
    private var currentObjectTypeId = "default_object_type"

    init {
        titleLabel.setAlignment(Align.center)
        addCloseButton()
        centerWindow()

        isModal = true
        isMovable = false

        defaults().pad(20f)

        val leftTable = VisTable()
        val objectTypeIdRow = VisTable()

        objectTypeIdRow.defaults().pad(0f, 0f, 10f, 0f)
        objectTypeIdRow.add(VisLabel("Object Type ID: "))
        objectTypeIdRow.add(objectIdTextField)

        val imageTable = VisTable()
        val imageIds = Array<String>()
        builder.images.keys.forEach { imageIds.add(it) }

        imageDropDown.selected = builder.objectTypes[currentObjectTypeId]?.image
        imageDropDown.items = imageIds

        imageDropDown.onChange {
            builder.objectTypes[currentObjectTypeId]?.image = imageDropDown.selected
            loadObjectType(currentObjectTypeId)
        }

        addImageButton.onClick { AnimationBuilderScreen.imageImporterWindow.toggle() }

        imageTable.add(VisLabel("Image: "))
        imageTable.add(imageDropDown)
        imageTable.add(addImageButton)

        val addObjectTypeButton = VisTextButton("Add")
        addObjectTypeButton.onClick {
            val objectTypeId = "new_object_type_$objectTypeCount"
            builder.objectTypes[objectTypeId] = KSLAnimationObject.ObjectType(objectTypeId, "default_object_type")
            objectTypeCount++

            loadObjectType(objectTypeId)
            updateObjectTypes()
        }

        updateIdButton.onClick {
            if (objectIdTextField.text != currentObjectTypeId) {
                builder.objectTypes[objectIdTextField.text] = KSLAnimationObject.ObjectType(objectIdTextField.text, builder.objectTypes[currentObjectTypeId]?.image!!)
                builder.objectTypes.remove(currentObjectTypeId)
                loadObjectType(objectIdTextField.text)
                updateObjectTypes()
            }
        }

        deleteObjectTypeButton.onClick {
            builder.objectTypes.remove(currentObjectTypeId)
            loadObjectType("default_object_type")
            updateObjectTypes()
        }

        val buttonTable = VisTable()
        buttonTable.defaults().pad(10f, 20f, 0f, 20f)
        buttonTable.add(updateIdButton)
        buttonTable.add(deleteObjectTypeButton)

        leftTable.add(objectTypeIdRow).row()
        leftTable.add(imageTable).row()
        leftTable.add(selectedObjectType).width(150f).height(150f).row()
        leftTable.add(buttonTable).row()
        add(leftTable).fillY().left()

        objectTypeAdapter.setItemClickListener { loadObjectType(it) }

        val rightTable = VisTable()
        rightTable.add(objectTypeView.mainTable).grow().row()
        rightTable.add(addObjectTypeButton)
        add(rightTable).fillY().right()
        fadeOut(true)
        loadObjectType("default_object_type")

        pack()
    }

    fun toggle() {
        if (open) fadeOut()
        else fadeIn()

        open = !open

        val imageIds = Array<String>()
        builder.images.keys.forEach { imageIds.add(it) }

        centerWindow()
        updateObjectTypes()

        imageDropDown.selected = builder.objectTypes[currentObjectTypeId]?.image
        imageDropDown.items = imageIds
    }

    private fun loadObjectType(objectTypeId: String) {
        objectIdTextField.text = objectTypeId

        println(objectTypeId + " " + builder.objectTypes[objectTypeId]?.image)
        selectedObjectType.setDrawable(builder.images[builder.objectTypes[objectTypeId]?.image]?.second)

        objectIdTextField.isDisabled = (objectTypeId == "default_object_type")
        updateIdButton.isDisabled = (objectTypeId == "default_object_type")
        deleteObjectTypeButton.isDisabled = (objectTypeId == "default_object_type")

        currentObjectTypeId = objectTypeId
    }

    private fun updateObjectTypes() {
        objectTypeAdapter.clear()
        objectTypeAdapter.addAll(ArrayList(builder.objectTypes.keys.toList()))
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
