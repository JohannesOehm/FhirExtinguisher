package fhirextinguisher

import ColumnsLexer
import ColumnsTokens
import ca.uhn.fhir.context.FhirContext
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


fun main() {
    val fhirContext = FhirContext.forR4()
    val wrapper = FhirPathEngineWrapperR4(fhirContext, fhirContext.newRestfulGenericClient(""))
    val parser = ColumnsParser(wrapper)

    println(parser.parseString("test@explodeLong(foo:Patient.name):Patient"))
    println(parser.parseString("test@explodeWide(\$disc:Patient.name):Patient"))

    println(parser.parseString("name@explodeWide(given:\$this.given[0],family:\$this.family,\$disc:iif(\$this.use.empty(\\)\\, 'foo'\\, \$this.use\\)):Patient.name"))
    println("emptyString=" + parser.parseString(""))

}


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
                "explodeLong" -> {
                    val subcolumns = parseSubColumns(columnType.typeParam()?.text ?: "")
                    ExplodeLong(subcolumns)
                }
                "explodeWide" -> {
                    val subcolumns = parseSubColumns(columnType.typeParam().text)
                    ExplodeWide(
                        subcolumns.find { it.name == "\$disc" }!!.expression,
                        subcolumns.filter { it.name != "\$disc" })
                }
                else -> {
                    throw RuntimeException("Cannot parse columnType '${columnType.text}' ('${ctx.text}')!")
                }
            }
        } else {
            Join(", ")
        }

    }

    private fun parseSubColumns(text: String): List<SubColumn> {
        return split(text.replace("\\)", ")")).map {
            split(it, ':').let {
                SubColumn(
                    it[0].replace("\\:", ":"),
                    fhirPathEngine.parseExpression(it[1])
                )
            }
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

    private fun split(toSplit: String, delimiter: Char = ',', escape: Char = '\\'): List<String> {
        val result = emptyList<String>().toMutableList()
        var lastStart = 0
        var escapeChar = false
        for ((i, c) in toSplit.withIndex()) {
            if (c == escape) {
                escapeChar = true
            } else {
                if (!escapeChar && c == delimiter) {
                    result += toSplit.substring(lastStart, i).replace("$escape$delimiter", "$delimiter")
                    lastStart = i + 1
                }
                if (i == toSplit.length - 1) {
                    result += toSplit.substring(lastStart).replace("$escape$delimiter", "$delimiter")
                }
                escapeChar = false
            }
        }
        return result
    }
}


