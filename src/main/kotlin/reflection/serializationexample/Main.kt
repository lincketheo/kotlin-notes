package reflection.serializationexample

fun serializationReflectionMain() {
    /**
     * 1. Be able to serialize data classes that extend device settings
     */
    val a = serialize(Movie("endgame", "marvel", 9.8f))
    println(a)

    /**
     * Be able to deserialize classes easily
     * by explicitly setting type name
     */
    val movie: Movie = serializableClass(a)
    println(movie)

    /**
     * Be able to deserialize classes easily
     * by specifying data type into abstract class
     */
    val movie2 = SerializableClassProvider.KeyedFactory.parseEntity(a, "Movie")
    println(movie2)

    /**
     * Be able to override serialization / deserialization
     */
    val b = serialize(Henry("hello", "world"))
    println(b)
    val henry: Henry = serializableClass(b)
    println(henry)

    /**
     * Easy additions
     */
    val aa = serialize(SettingNew("hello", 5))
    print(aa)
    val aaa : SettingNew = serializableClass(aa)
    print(aaa)

}
