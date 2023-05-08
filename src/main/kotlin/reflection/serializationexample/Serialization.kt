package reflection.serializationexample

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

inline fun <reified T : SerializableClass> serialize(
    value: T,
    objectMapper: ObjectMapper = jacksonObjectMapper(),
) : String {
    val serializer = T::class.companionObject
        ?.functions
        ?.firstOrNull { it.name == "provideSerializer" }
        ?.call(T::class.companionObjectInstance)
            as? SerializableClassSerializer.Serializer

    return serializer?.serialize(value)
        ?: objectMapper.writeValueAsString(value)
}