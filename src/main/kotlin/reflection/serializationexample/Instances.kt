package reflection.serializationexample


data class Movie(
    val name: String,
    val studio: String,
    val rating: Float,
) : SerializableClass

data class SettingNew(
    val thingy: String,
    val abc: Int,
): SerializableClass

data class Henry(
    val type: String,
    val type2: String,
) : SerializableClass {
    companion object {
        fun provideFactory(): SerializableClassProvider.Factory {
            return object : SerializableClassProvider.Factory {
                override fun <T : SerializableClass> deserialize(serializedValue: String): T {
                    @Suppress("UNCHECKED_CAST")
                    return Henry(serializedValue, "World") as T
                }
            }
        }

        fun provideSerializer(): SerializableClassSerializer.Serializer {
            return object : SerializableClassSerializer.Serializer {
                override fun <T : SerializableClass> serialize(value: T): String {
                    return with(value as Henry) { type + type2 }
                }
            }
        }
    }
}
