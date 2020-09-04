import ca.uhn.fhir.context.FhirContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.model.Bundle
import wrappers.FhirPathEngineWrapper
import wrappers.FhirPathEngineWrapperR4
import java.io.FileReader
import kotlin.math.max


fun main() {

    val forR4 = FhirContext.forR4()
    val fpe = forR4.let { FhirPathEngineWrapperR4(it, it.newRestfulGenericClient("")) }
    val parser = forR4.newJsonParser()
    val bundle =
        parser.parseResource(FileReader("C:\\Users\\Johannes\\IdeaProjects\\fhirextinguisher\\src\\main\\resources\\test-patient.json")) as Bundle


    val c = Column(
        "name", fpe.parseExpression("Patient.name"), ExplodeLong(
            listOf(
                SubColumn("use", fpe.parseExpression("\$this.use")),
                SubColumn("given", fpe.parseExpression("\$this.given")),
                SubColumn("family", fpe.parseExpression("\$this.family"))
            )
        )
    )
    val c2 = Column(
        "name", fpe.parseExpression("Patient.name"), ExplodeWide(
            discriminator = fpe.parseExpression("%index"),
            subcolumns = listOf(
                SubColumn("", fpe.parseExpression("\$this.given")),
                SubColumn("family", fpe.parseExpression("\$this.family"))
            )
        )
    )
    val c1 = Column("name", fpe.parseExpression("Patient.name.given"), ExplodeLong(emptyList()))
    val c4 = Column("test", fpe.parseExpression("2"), ExplodeLong(emptyList()))

//    rt.addColumn(c, bundle.entry[0].resource, fpe)

    val subtables = mutableListOf<SubTable>()

    for (entryComponent in bundle.entry) {
        val st = SubTable()
        st.addColumn(c2, entryComponent.resource, fpe)
        st.addColumn(c1, entryComponent.resource, fpe)
        st.addColumn(c4, entryComponent.resource, fpe)
        println(st)
        subtables += st
    }

    ResultTable(subtables).print(CSVFormat.EXCEL.printer())

}


class SubTable() {
    val data = LinkedHashMap<Pair<Int, String>, List<String?>>()
    var currentLength = 1
    var colIdx = 1

    fun addColumn(column: Column, base: IBase, fpe: FhirPathEngineWrapper) {
        val eval = fpe.evaluateToBase(base, column.expression)
        when (column.listProcessingMode) {
            is Singleton -> {
                val value = eval.single()
                this.data[colIdx to column.name] = List(currentLength) { fpe.convertToString(value) }
            }
            is Join -> {
                val value = eval.joinToString(column.listProcessingMode.delimiter) { fpe.convertToString(it) }
                this.data[colIdx to column.name] = List(currentLength) { value }
            }
            is ExplodeLong -> {
                val sc = if (column.listProcessingMode.subcolumns.isEmpty()) {
                    listOf(column.name to fpe.parseExpression("\$this"))
                } else {
                    column.listProcessingMode.subcolumns.map { column.name + "." + it.name to it.expression }
                }

                when {
                    eval.size == 1 -> {
                        for (s in sc) {
                            //TODO: Handle multiple return values: Throw error?
                            val subitem = fpe.evaluateToBase(eval[0], s.second)
                            this.data[colIdx to s.first] = List(currentLength) {
                                if (subitem.size != 1) null else fpe.convertToString(subitem.first())
                            }
                        }
                    }
                    eval.isEmpty() -> {
                        for (s in sc) {
                            this.data[colIdx to s.first] = List(currentLength) { null }
                        }
                    }
                    else -> {
                        for ((i, col) in this.data) {
                            this.data[i] = repeatList(col, eval.size)
                        }
                        for (s in sc) {
                            val result: List<String?> = eval.map { fpe.evaluateToBase(it, s.second).firstOrNull() }
                                .map { if (it != null) fpe.convertToString(it) else null }
                            this.data[colIdx to s.first] = stretchList(result, currentLength)
                        }
                        this.currentLength = currentLength * eval.size
                    }
                }
            }
            is ExplodeWide -> {
                for ((index, iBase) in eval.withIndex()) {
                    val disc =
                        fpe.evaluateToBase(iBase, column.listProcessingMode.discriminator, mapOf("index" to index))
                            .firstOrNull()?.let { fpe.convertToString(it) } ?: "empty"
                    val sc = if (column.listProcessingMode.subcolumns.isNotEmpty()) {
                        column.listProcessingMode.subcolumns.map { (column.name + "." + disc + if (it.name != "") ".${it.name}" else "") to it.expression }
                    } else {
                        listOf(column.name + "." + disc to fpe.parseExpression("\$this"))
                    }
                    for (s in sc) {
                        val result: String? =
                            fpe.evaluateToBase(iBase, s.second, mapOf("index" to index)).map { fpe.convertToString(it) }
                                .firstOrNull()
                        this.data[colIdx to s.first] = List(currentLength) { result }
                    }
                }
            }
        }
        colIdx++
    }


    fun addColumn(columnName: String, value: String) {
        this.data[colIdx to columnName] = List(currentLength) { value }
        colIdx++
    }


    private fun repeatList(col: List<String?>, times: Int): List<String?> {
        val list = ArrayList<String?>(col.size * times)
        repeat(times) {
            list.addAll(col)
        }
        return list
    }


    private fun stretchList(col: List<String?>, times: Int): List<String?> {
        val list = ArrayList<String?>(col.size * times)
        for (item in col) {
            repeat(times) {
                list.add(item)
            }
        }
        return list
    }


    override fun toString(): String {
        val maxLength = mutableMapOf<String, Int>()
        for ((r, value) in data.entries) {
            val columnName = r.second
            maxLength[columnName] = max(columnName.length, value.map { it?.length ?: "null".length }.max()!!)
        }
        return buildString {
            for (key in data.keys) {
                val columnName = key.second
                append(columnName).append(" ".repeat(maxLength[columnName]!! - columnName.length)).append(" | ")
            }
            append("\n")
            append("-".repeat(maxLength.values.sum() + maxLength.size * " | ".length))
            append("\n")
            for (i in data.values.first().indices) {
                for ((key, value) in data) {
                    val columnName = key.second
                    append(value[i]).append(" ".repeat(maxLength[columnName]!! - (value[i]?.length ?: 4))).append(" | ")
                }
                append("\n")
            }
        }
    }
}


class ResultTable(val subtables: List<SubTable>) {

    fun print(printer: CSVPrinter) {
        val colNames = getAllColumnNames()
        printer.printRecord(colNames.map { it.second })

        for (subtable in subtables) {
            for (i in subtable.data.values.first().indices) {
                for (colName in colNames) {
                    printer.print(subtable.data[colName]?.get(i))
                }
                printer.println()
            }
        }


    }

    private fun getAllColumnNames(): List<Pair<Int, String>> {
        return this.subtables.map { it.data.keys }.flatten().sortedBy { it.first }.distinct()
    }


}