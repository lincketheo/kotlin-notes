package reflection.serializationexample

/**
 * A class that is serializable
 */
interface SerializableClass

/**
 * Provides factories to construct [SerializableClass]'s
 */
class SerializableClassProvider {
    interface Factory {
        fun <T : SerializableClass> deserialize(serializedValue: String): T
    }

    object KeyedFactory {
        fun parseEntity(serializedValue: String, dataKey: String): SerializableClass {
            return when (dataKey) {
                "Henry" -> serializableClass<Henry>(serializedValue)
                "Movie" -> serializableClass<Movie>(serializedValue)
                "SettingNew" -> serializableClass<SettingNew>(serializedValue)
                else -> {
                    throw IllegalArgumentException("Invalid class name $dataKey")
                }
            }
        }
    }
}

class SerializableClassSerializer {
    interface Serializer {
        fun <T : SerializableClass> serialize(value: T): String
    }
}