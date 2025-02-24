package ksl.animation.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ksl.animation.setup.KSLAnimation
import ksl.animation.setup.KSLAnimationObject

@Serializable
data class Position(val x: Double, val y: Double)

@OptIn(ExperimentalSerializationApi::class)
fun parseJsonToAnimation(content: String): KSLAnimation {
    val json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        serializersModule = SerializersModule {
            polymorphic(KSLAnimationObject::class) {
                subclass(KSLAnimationObject.Image::class, KSLAnimationObject.Image.serializer())
                subclass(KSLAnimationObject.Queue::class, KSLAnimationObject.Queue.serializer())
                subclass(KSLAnimationObject.Resource::class, KSLAnimationObject.Resource.serializer())
                subclass(KSLAnimationObject.ObjectType::class, KSLAnimationObject.ObjectType.serializer())
                subclass(KSLAnimationObject.Object::class, KSLAnimationObject.Object.serializer())
                subclass(KSLAnimationObject.Station::class, KSLAnimationObject.Station.serializer())
            }
        }
    }

    return json.decodeFromString<KSLAnimation>(content)
}
