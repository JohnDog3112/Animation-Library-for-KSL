package ksl.animation.setup

import com.badlogic.gdx.graphics.Texture
import kotlinx.serialization.Serializable

@Serializable
data class ResourceState(val name: String, val image: String, val default: Boolean = false)

@Serializable
class ResourceStates(private val states: List<ResourceState>) {
    companion object { const val DEFAULT_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAADLklEQVRoQ92aOWgVQRyH9yloo4JYeBE0JJGAIAgqWglRsBEU8ewSDIKFBBu1EIxaxUaDIoKC2nkh2hlQwUoxgpVXkhcL70LEo1DB4/s/ZsJm3x6zm/fyZmbgx+Rl5/h9OzO7szNTCmoU/gXBCopaiZag+UoLVCy1fEDvVfyWuIwGS0HwpBYWKKd4wHwTufehbjS7YElfyHcencHMm4JlBIVAAFhOhQfRVjS1aOWRfH/4fR0dx9TzvGXmAgFgHhX0o+15K8qZ/grp92Puo2k+IxAApqgudIx4lmnhE0z3lfyH0VlM/s0qKxMEiGkUcgL1ZBVWp+unpBtj9Hda+akgQMwk8020vk4mTYsdkO6M2W9JGRJBgFhEpjuo3bS2OqeTB8BGDL+OqycWBIgZJL6P5L1gU3iMmXWY/hE1lQQiT40dNhGEvFzF9M5MEFqjk0QXLYXQtrqAuRT2OK5FgJApxSskXcvmIF2rHfPvtMkoyC0ubLKZIOTtNuY3V4HQGqv550NHILTNNcA8kh9jLQKIS62hQcZapQICRCvRUBjMkZbBemWsDGmQXv5xxBHzUZtHgejVIKNcbXYUZBSIlhJtsxiA2Ne+Q2DNAtKJYdtfgFn3tEtAZJrcqCl6lkHT6/0CIjPcDaY5LE03ICAjmGux1KCprbKAyHfxXNMclqb7JCA/MTfdUoOmtn55BeJN1/JmsHvz+PXmhejNFMWPSaM8qB1/u5eZxrd692Eln7rDpq9Ry9K10Rojfi0+qHHix3KQgnFpSSh+gU6BLCR+iWxfMv2Mx2WMC9klroSq1XhHvuHTF7E1HTA2bytc5u7LbGRcSNvouUfKVZY9agfx02G80aPGyxziB2ipJTDP8LEWCBkfVSFxD1HByFb0NdToVZa7eNiC2e9JNzUVJDRm+vj7QINapg+Th7LqNgJRrbOL+ByarAMDshW9G4M3siDkujGIgpEjHCdR1WakSWU50kh37sFcbY9wRA2oQzXS1bahWh6qkbsvXelpDuhK0lwtEgMkhwr2oj1oIsecLpD/NGYm95hT3N2ilfTBszau6wNnSQfP5BDaCyQHz3Lf/bj6/wNIW7ItAq1QegAAAABJRU5ErkJggg==" }

    val defaultState: ResourceState;

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
