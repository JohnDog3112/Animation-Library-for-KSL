package ksl.animation.setup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ksl.animation.util.Position

@Serializable
data class KSLAnimation(
    val objects: List<KSLAnimationObject>
)

@Serializable
sealed class KSLAnimationObject {
    @Serializable
    @SerialName("image")
    data class Image(val id: String, val data: String) : KSLAnimationObject()

    @Serializable
    @SerialName("queue")
    data class Queue(val id: String, val startPosition: Position, val endPosition: Position, val scale: Double = 1.0) : KSLAnimationObject()

    @Serializable
    @SerialName("resource")
    data class Resource(val id: String, val states: ArrayList<ResourceState>, val position: Position = Position(0.0, 0.0), val width: Double = 1.0, val height: Double = 1.0) : KSLAnimationObject()

    @Serializable
    @SerialName("object_type")
    data class ObjectType(val id: String, var image: String) : KSLAnimationObject()

    @Serializable
    @SerialName("object")
    data class Object(val id: String, val objectType: String, val position: Position = Position(0.0, 0.0), val width: Double = 1.0, val height: Double = 1.0) : KSLAnimationObject()

    @Serializable
    @SerialName("station")
    data class Station(val id: String, val position: Position = Position(0.0, 0.0)) : KSLAnimationObject()

    @Serializable
    @SerialName("variable")
    data class Variable(
        val id: String,
        val position: Position = Position(0.0, 0.0),
        val width: Double = 1.0,
        val height: Double = 1.0,
        val maxTextScale: Double = 2.0,
        val defaultValue: String = "0",
        val precision: Int = 2,
        val textColor: String = "#FFFFFF"
    ) : KSLAnimationObject()
}
