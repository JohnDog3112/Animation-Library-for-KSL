package ksl.animation.common.renderables

import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.*
import ksl.animation.builder.AnimationBuilderScreen
import ksl.animation.builder.changes.EditObjectSettingsChange
import ksl.animation.common.AnimationScene
import ksl.animation.setup.KSLAnimationObject
import ksl.animation.sim.MovementFunctions
import ksl.animation.sim.events.MoveQuery
import ksl.animation.util.Position
import ktx.actors.onChange
import ktx.actors.onClick

class KSLObject(
    id: String,
    position: Position,
    var objectType: String,
    width: Double = 1.0,
    height: Double = 1.0
) : KSLResizable(id, position, width, height) {
    constructor(kslObject: KSLAnimationObject.Object) : this(kslObject.id, kslObject.position, kslObject.objectType, kslObject.width, kslObject.height)
    private val objectIdTextField = VisTextField(id)
    private val objectTypeDropDown = VisSelectBox<String>()
    private val addObjectTypeButton = VisTextButton("+")

    fun serialize(): KSLAnimationObject.Object {
        return KSLAnimationObject.Object(
            this.id,
            this.objectType,
            this.position,
            this.width,
            this.height
        )
    }
    var inQueue = false

    fun render(x: Double, y: Double, width: Double, height: Double, scene: AnimationScene) {
        val translatedPosition = scene.worldToScreen(Position(x, y))
        val translatedSize = Position(width, height) * scene.screenUnit

        val objectType = scene.objectTypes[this.objectType]
        if (objectType != null) {
            val texture = scene.images[objectType.image]?.second
            if (texture != null) {
                scene.spriteBatch.begin()
                scene.spriteBatch.draw(texture, translatedPosition.x.toFloat(), translatedPosition.y.toFloat(), translatedSize.x.toFloat(), translatedSize.y.toFloat())
                scene.spriteBatch.end()
            } else {
                throw RuntimeException("Image ${objectType.image} is not found")
            }
        } else {
            throw RuntimeException("Base object ${this.objectType} is not found")
        }

        super.render(scene)
    }

    override fun openEditor(scene: AnimationScene, content: VisTable) {
        val idTable = VisTable()
        idTable.add(VisLabel("Object ID: "))
        idTable.add(objectIdTextField)
        content.add(idTable).row()

        val objectTypeTable = VisTable()
        val objectTypeIds = Array<String>()
        scene.objectTypes.keys.forEach { objectTypeIds.add(it) }

        objectTypeDropDown.items = objectTypeIds

        objectTypeDropDown.onChange {
            objectType = objectTypeDropDown.selected
        }

        addObjectTypeButton.onClick {
            AnimationBuilderScreen.objectTypeEditorWindow.toggle()
        }

        objectTypeTable.add(VisLabel("Object Type: "))
        objectTypeTable.add(objectTypeDropDown).row()
        objectTypeTable.add(addObjectTypeButton)
        content.add(objectTypeTable).row()

        super.openEditor(scene, content)
    }

    override fun closeEditor(scene: AnimationScene) {
        if (id != objectIdTextField.text) {
            scene.applyChange(EditObjectSettingsChange(
                scene, this,
                id, objectIdTextField.text,
                objectType, objectTypeDropDown.selected
            ))
        }
    }

    override fun render(scene: AnimationScene) {
        if (!inQueue) {
            this.render(position.x, position.y, width, height, scene)
        }
    }

    fun applyMovement(movement: MoveQuery): Boolean {
        val amount = movement.elapsedTime / movement.duration
        if (amount >= 1 || movement.movementFunction == MovementFunctions.INSTANT_FUNCTION) {
            position = movement.endPosition
            return true
        } else {
            position = ((movement.endPosition - movement.startPosition) * MovementFunctions.applyFunction(
                movement.movementFunction,
                amount
            )) + movement.startPosition
            return false
        }
    }
}
