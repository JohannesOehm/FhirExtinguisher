import com.strumenta.antlrkotlin.examples.ColumnsLexer
import com.strumenta.antlrkotlin.examples.ColumnsTokens
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import kotlin.js.JsExport


@JsExport
fun stringifyColumns(columns: Array<Column>): String {
    return columns.joinToString(",")
}

@JsExport
fun parseColumns(inputStr: String): Array<Column> {
    val input = CharStreams.fromString(inputStr)
    val lexer = ColumnsLexer(input)
    val parser = ColumnsTokens(CommonTokenStream(lexer))

    val result = mutableListOf<Column>()
    try {
        val root = parser.columns()
        for (column in root.findColumn()) {
            result += Column(
                name = column.findColumnName()!!.text.replace("\\:", ":").replace("\\\\", "\\"),
                type = column.findColumnType()?.let { parseType(it) },
                expression = column.findFhirpathExpression()!!.text.replace("\\@", "@").replace("\\,", ",")
                    .replace("\\\\", "\\")
            )
        }
    } catch (e: Throwable) {
        println("Error: $e")
    }
    return result.toTypedArray()
}

private fun parseType(columnType: ColumnsTokens.ColumnTypeContext): ListProcessingMode? {
    return when (columnType.findTypeName()?.text) {
        "singleton" -> Singleton
        "join" -> {
            val separator = columnType.findTypeParam()?.text?.drop(1)?.dropLast(1)
            Join(separator ?: ", ") //TODO: support \n as separator
        }
        "explodeLong" -> {
            ExplodeLong(parseColumns(columnType.findTypeParam()?.text ?: ""))
        }
        "explodeWide" -> {
            val subColumns = parseColumns(columnType.findTypeParam()?.text ?: "")
            ExplodeWide(
                discriminator = subColumns.find { it.name == "\$disc" }!!.expression,
                subcolumns = subColumns.filter { it.name != "\$disc" }.toTypedArray()
            )
        }
        null -> null
        else -> {
            throw RuntimeException("Cannot parse columnType '${columnType.text}'!")
        }
    }
}

