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
        val type = if (ctx.columnType() != null) {
            ctx.columnType().text
        } else {
            "join(\" \")"
        }
        return Column(columnName, expression, type)
    }

    public fun stringifyList(columns: List<Column>): String {
        return columns.joinToString(",") { stringifyColumn(it) }
    }

    private fun stringifyColumn(column: Column): String {
        val nameEscaped = column.name.replace(":", "\\:").replace("@", "\\@")
        val expressionStrEscaped = column.expression.toString().replace(",", "\\,")

        if (column.listProcessingMode != null) {
            val typeEscaped = column.listProcessingMode.replace(":", "\\:")
            return "$nameEscaped@$typeEscaped:$expressionStrEscaped"
        } else {
            return "$nameEscaped:$expressionStrEscaped"
        }
    }
}


