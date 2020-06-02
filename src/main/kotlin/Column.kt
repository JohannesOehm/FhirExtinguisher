import wrappers.ExpressionWrapper

data class Column(
    val name: String,
    val expression: ExpressionWrapper,
    val listProcessingMode: ListProcessingMode
)


abstract class ListProcessingMode

class Join(val delimiter: String) : ListProcessingMode() {
    override fun toString(): String {
        return "join(\"$delimiter\")"
    }
}

class Explode : ListProcessingMode() {
    override fun toString(): String {
        return "explode"
    }
}

class Singleton : ListProcessingMode() {
    override fun toString(): String {
        return "singleton"
    }
}