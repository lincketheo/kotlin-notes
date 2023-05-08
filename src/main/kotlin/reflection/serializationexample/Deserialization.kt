package reflection.serializationexample

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

/**
 * A deserializer that checks if there is a provider,
 * if not, uses default deserializer
 */
inline fun <reified T : SerializableClass> serializableClass(
    serializedValue: String,
    objectMapper: ObjectMapper = jacksonObjectMapper(),
): T {
    /**
     * Try to find an overridden factory within the companion
     * object named provideFactory
     */
    val factory = T::class.companionObject
        ?.functions
        ?.firstOrNull { it.name == "provideFactory" }
        ?.call(T::class.companionObjectInstance)
            as? SerializableClassProvider.Factory

    /**
     * If no overrides exist, default to just plain old object mapper
     */
    return factory?.deserialize(serializedValue)
        ?: objectMapper.readValue(serializedValue, T::class.java) as T
}

