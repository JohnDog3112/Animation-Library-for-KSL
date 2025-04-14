package ksl.animation.setup

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import kotlinx.serialization.Serializable

@Serializable
data class ResourceState(var name: String, var image: String, var default: Boolean = false)

@Serializable
class ResourceStates(var states: ArrayList<ResourceState>) {
    val defaultState: ResourceState

    init {
        val default = states.find { it.default }
        if (default != null) {
            defaultState = default
        } else {
            throw RuntimeException("No default state found")
        }
    }

    fun getState(stateName: String): ResourceState {
        val state = states.find { it.name == stateName }
        if (state == null) throw RuntimeException("No state found")
        return state
    }

    fun addState(stateName: String) {
        states.add(ResourceState(stateName, "default_resource", false))
    }

    fun updateState(oldStateName: String, newState: ResourceState) {
        val state = getState(oldStateName)

        state.name = newState.name
        state.image = newState.image
        state.default = newState.default

        if (newState.default) setDefaultState(newState.name)
    }

    fun removeState(stateName: String) {
        val state = getState(stateName)
        states.remove(state)
    }

    private fun setDefaultState(stateName: String) {
        states.forEach { it.default = false }
        val state = getState(stateName)
        state.default = true
    }

    fun getImage(stateName: String, images: Map<String, Pair<Pixmap, Texture>>): Texture {
        val neededState = states.find { it.name == stateName }
        val imageName = neededState?.image ?: defaultState.image
        val texture = images[imageName]

        if (texture != null) return texture.second
        else throw RuntimeException("Texture \"${imageName}\" not found in assets!")
    }
}
