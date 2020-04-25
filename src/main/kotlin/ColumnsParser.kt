import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.CharStreams
import wrappers.FhirPathEngineWrapper
import java.lang.Exception

class ColumnsParser(private val fhirPathEngine: FhirPathEngineWrapper) {

    public fun parseString(string: String): List<Column> {
        val input = CharStreams.fromString(string)
        val lexer = ColumnsLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parseTree = ColumnsTokens(tokens)
        return visitColumns(parseTree.columns())
    }


    private fun visitColumns(ctx: ColumnsTokens.ColumnsContext): List<Column> {
        return ctx.column().map { processColumn(it) }.toList()
    }


    private fun processColumn(ctx: ColumnsTokens.ColumnContext): Column {
        val columnName = ctx.columnName().text.replace("\\:", ":").replace("\\@", "@")
        val expressionStr = ctx.fhirpathExpression().text.replace("\\,", ",")

        val expression = try {
            fhirPathEngine.parseExpression(expressionStr)
        } catch (e: Exception) {
            throw RuntimeException("Error parsing FHIRPath-Expression: $expressionStr", e)
        }
        val type = parseType(ctx)
        return Column(columnName, expression, type)
    }

    private fun parseType(ctx: ColumnsTokens.ColumnContext): ListProcessingMode {
        val columnType = ctx.columnType()
        return if (columnType != null) {
            when (columnType.typeName().text) {
                "join" -> {
                    val string = columnType.typeParam().text
                    //TODO
                    Join(string.drop(1).dropLast(1))
                }
                "explode" -> {
                    Explode()
                }
                else -> {
                    throw RuntimeException("Cannot parse columnType '${columnType.text}' ('${ctx.text}')!")
                }
            }
        } else {
            Join(", ")
        }

    }

    public fun stringifyList(columns: List<Column>): String {
        return columns.joinToString(",") { stringifyColumn(it) }
    }

    private fun stringifyColumn(column: Column): String {
        val nameEscaped = column.name.replace(":", "\\:").replace("@", "\\@")
        val expressionStrEscaped = column.expression.toString().replace(",", "\\,")

        if (column.listProcessingMode != null) {
            val typeEscaped = column.listProcessingMode.toString().replace(":", "\\:")
            return "$nameEscaped@$typeEscaped:$expressionStrEscaped"
        } else {
            return "$nameEscaped:$expressionStrEscaped"
        }
    }
}


