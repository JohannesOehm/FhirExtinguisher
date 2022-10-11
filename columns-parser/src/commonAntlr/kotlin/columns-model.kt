import kotlin.js.JsExport

@JsExport
data class Column(
    val name: String,
    val expression: String,
    val type: ListProcessingMode? = null,
) {
    override fun toString() = buildString {
        append(name.replace("\\", "\\\\").replace(":", "\\:"))
        append(":")
        append(expression.replace("\\", "\\\\").replace("@", "\\@").replace(",", "\\,"))
        if (type != null) {
            append("@")
            append(type)
        }
    }
}

@JsExport
sealed class ListProcessingMode

/**
 * join strings into a single cell
 */
@JsExport
data class Join(val delimiter: String) : ListProcessingMode() {
    override fun toString() = "join(\"${delimiter.replace("\n", "\\n")}\")"
}

/**
 * create new column for each entry
 */
@JsExport
data class ExplodeWide(val discriminator: String, val subcolumns: Array<Column>) : ListProcessingMode() {
    override fun toString() =
        "explodeWide(" + Column("\$disc", discriminator) + "," + subcolumns.joinToString(",") + ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ExplodeWide

        if (discriminator != other.discriminator) return false
        if (!subcolumns.contentEquals(other.subcolumns)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = discriminator.hashCode()
        result = 31 * result + subcolumns.contentHashCode()
        return result
    }
}

/**
 * create new row for each entry
 */
@JsExport
data class ExplodeLong(val subcolumns: Array<Column>) : ListProcessingMode() {
    override fun toString() = "explodeLong(" + subcolumns.joinToString(",") + ")"

    //TODO: Brauche ich das überhaupt? Ist schließlich eine data-class
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ExplodeLong

        if (!subcolumns.contentEquals(other.subcolumns)) return false

        return true
    }

    override fun hashCode(): Int {
        return subcolumns.contentHashCode()
    }
}

@JsExport
object Singleton : ListProcessingMode() {
    override fun toString() = "singleton"
}