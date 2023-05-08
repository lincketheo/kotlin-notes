interface GenericClass<T>

fun fromString(value: String): GenericClass<>

class Implementation1 : GenericClass<Int>
class Implementation2 : GenericClass<String>

fun genericsMain() {

}