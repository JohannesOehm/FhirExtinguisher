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

    return buildList {
        try {
            val root = parser.columns()
            for (column in root.column()) {
                add(
                    Column(
                        name = (column.columnName()?.text ?: "").replace("\\:", ":").replace("\\\\", "\\"),
                        type = column.columnType()?.let { parseType(it) },
                        expression = column.fhirpathExpression()!!.text.replace("\\@", "@").replace("\\,", ",")
                            .replace("\\\\", "\\")
                    )
                )
            }
        } catch (e: Throwable) {
            println("Error: $e")
        }
    }.toTypedArray()
}

private fun parseType(columnType: ColumnsTokens.ColumnTypeContext): ListProcessingMode? {
    return when (columnType.typeName()?.text) {
        "singleton" -> Singleton
        "join" -> {
            val separator = columnType.typeParam()?.text?.drop(1)?.dropLast(1)
            Join(separator?.replace("\\n", "\n") ?: ", ") //TODO: support \n as separator
        }

        "explodeLong" -> ExplodeLong(parseColumns(columnType.typeParam()?.text ?: ""))
        "explodeWide" -> {
            val subColumns = parseColumns(columnType.typeParam()?.text ?: "")
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

