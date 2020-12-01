package fhirextinguisher

data class Column(
    val name: String,
    val expression: ExpressionWrapper,
    val listProcessingMode: ListProcessingMode
)

data class SubColumn(val name: String, val expression: ExpressionWrapper)

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

/**
 *
 */
/**
 * Erzeuge neuen Entry für jede Spalte
 */
class ExplodeWide(val discriminator: ExpressionWrapper, val subcolumns: List<SubColumn>) : ListProcessingMode() {
    override fun toString(): String {
        return "explodeWide"
    }


}

/**
 * Erzeuge neuen Entry für jede Spalte
 */
class ExplodeLong(val subcolumns: List<SubColumn>) : ListProcessingMode() {
    override fun toString(): String {
        return "explodeLong"
    }
}


class Singleton : ListProcessingMode() {
    override fun toString(): String {
        return "singleton"
    }
}