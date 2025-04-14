package ksl.animation.common.renderables

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.VisCheckBox
import com.kotcrab.vis.ui.widget.VisImage
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisTextField
import ksl.animation.builder.AnimationBuilderScreen
import ksl.animation.builder.changes.EditResourceSettingsChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.setup.ResourceState
import ksl.animation.setup.ResourceStates
import ksl.animation.util.Position
import ktx.actors.onChange
import ktx.actors.onClick

class KSLResource(id: String, position: Position, states: ArrayList<ResourceState>, width: Double = 1.0, height: Double = 1.0) : KSLResizable(id, position, width, height) {
    private val resourceIdTextField = VisTextField(id)
    private val statesDropDown = VisSelectBox<String>()
    private val stateNameField = VisTextField()
    private val stateDefaultCheckbox = VisCheckBox("Default?")
    private val addImageButton = VisTextButton("+")
    private val imagesDropDown = VisSelectBox<String>()
    private val imagePreview = VisImage()
    private val addStateButton = VisTextButton("Add State")
    private val updateStateButton = VisTextButton("Update State")
    private val deleteStateButton = VisTextButton("Delete State")
    private var stateCount = 0

    constructor(resourceObject: KSLAnimationObject.Resource) : this(resourceObject.id, resourceObject.position, resourceObject.states, resourceObject.width, resourceObject.height)

    fun serialize(): KSLAnimationObject.Resource {
        return KSLAnimationObject.Resource(
            this.id,
            this.resourceStates.states,
            this.position,
            this.width,
            this.height
        )
    }

    val resourceStates = ResourceStates(states)
    private var currentState = resourceStates.defaultState.name

    fun setState(stateName: String, scene: AnimationScene? = null) {
        currentState = stateName
        if (scene != null) {
            val state = resourceStates.getState(stateName)
            statesDropDown.selected = stateName
            stateNameField.text = stateName
            stateDefaultCheckbox.isChecked = state.default == true
            imagesDropDown.selected = state.image
            imagePreview.setDrawable(scene.images.let { resourceStates.getImage(stateName, it) })

            deleteStateButton.isDisabled = state.default == true
        }
    }

    private fun addState() {
        val stateName = "new_state_$stateCount"
        stateCount++
        resourceStates.addState(stateName)
        updateDropdown()
    }

    private fun updateDropdown() {
        val stateNames = Array<String>()
        resourceStates.states.forEach { stateNames.add(it.name) }
        statesDropDown.items = stateNames
    }

    override fun openEditor(scene: AnimationScene, content: VisTable) {
        val idTable = VisTable()
        idTable.add(VisLabel("Resource ID: "))
        idTable.add(resourceIdTextField)
        content.add(idTable).row()

        setState(currentState, scene)

        updateDropdown()
        statesDropDown.onChange { setState(statesDropDown.selected, scene) }

        val statesTable = VisTable()
        val statesDropdownTable = VisTable()
        statesDropdownTable.add(VisLabel("States: "))
        statesDropdownTable.add(statesDropDown)

        val stateNameTable = VisTable()
        stateNameTable.add(VisLabel("State Name: "))
        stateNameTable.add(stateNameField).width(100f).row()

        addImageButton.onClick { AnimationBuilderScreen.imageImporterWindow.toggle() }

        val stateImageTable = VisTable()
        val imageIds = Array<String>()
        scene.images.keys.forEach { imageIds.add(it) }

        imagesDropDown.items = imageIds

        stateImageTable.add(VisLabel("State Image: "))
        stateImageTable.add(imagesDropDown)
        stateImageTable.add(addImageButton)

        val buttonTable = VisTable()
        buttonTable.defaults().pad(10f, 5f, 0f, 5f)

        addStateButton.onClick { addState() }
        updateStateButton.onClick {
            resourceStates.updateState(currentState, ResourceState(stateNameField.text, imagesDropDown.selected, stateDefaultCheckbox.isChecked))
            currentState = stateNameField.text
            updateDropdown()
        }
        deleteStateButton.onClick {
            resourceStates.removeState(currentState)
            currentState = resourceStates.defaultState.name
        }

        buttonTable.add(addStateButton)
        buttonTable.add(updateStateButton)
        buttonTable.add(deleteStateButton)

        statesTable.add(statesDropdownTable).row()
        statesTable.add(stateNameTable).row()
        statesTable.add(stateDefaultCheckbox).row()
        statesTable.add(stateImageTable).row()
        statesTable.add(imagePreview).width(100f).height(100f).row()
        statesTable.add(buttonTable).row()
        content.add(statesTable).row()

        super.openEditor(scene, content)
    }

    override fun closeEditor(scene: AnimationScene) {
        if (id != resourceIdTextField.text) {
            scene.applyChange(EditResourceSettingsChange(
                scene, this,
                id, resourceIdTextField.text
            ))
        }
    }

    override fun render(scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(position)
        val translatedSize = Position(width, height) * scene.screenUnit

        val texture: Texture = resourceStates.getImage(currentState, scene.images)
        scene.spriteBatch.begin()
        scene.spriteBatch.draw(
            texture,
            translatedPosition.x.toFloat(),
            translatedPosition.y.toFloat(),
            translatedSize.x.toFloat(),
            translatedSize.y.toFloat()
        )
        scene.spriteBatch.end()

        super.render(scene)
    }
}
