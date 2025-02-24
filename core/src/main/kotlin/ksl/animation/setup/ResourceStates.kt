package ksl.animation.setup

import com.badlogic.gdx.graphics.Texture
import kotlinx.serialization.Serializable

@Serializable
data class ResourceState(val name: String, val image: String, val default: Boolean = false)

@Serializable
class ResourceStates(private val states: List<ResourceState>) {
    private val defaultState: ResourceState;

    init {
        val default = states.find { it.default }
        if (default != null) {
            defaultState = default
        } else {
            throw RuntimeException("No default state found")
        }
    }

    fun getImage(state: String, images: Map<String, Texture>): Texture {
        val neededState = states.find { it.name == state }
        val imageName = neededState?.image ?: defaultState.image
        val texture = images[imageName]

        if (texture != null) return texture
        else throw RuntimeException("Texture \"${imageName}\" not found in assets!")
    }
}
