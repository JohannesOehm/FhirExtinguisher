package fhirextinguisher

import Column
import ExplodeLong
import ExplodeWide
import Join
import Singleton
import ca.uhn.fhir.context.FhirContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.model.Bundle
import java.io.FileReader
import kotlin.math.max


fun main() {
    val forR4 = FhirContext.forR4()
    val fpe = forR4.let { FhirPathEngineWrapperR4(it, it.newRestfulGenericClient("")) }
    val parser = forR4.newJsonParser()
    val bundle =
        parser.parseResource(FileReader("C:\\Users\\oehmj\\IdeaProjects\\fhirextinguisher\\src\\main\\resources\\test-patient.json")) as Bundle


    val c = Column(
        "name", "Patient.name", ExplodeLong(
            arrayOf(
                Column("use", "\$this.use"),
                Column("given", "\$this.given"),
                Column("family", "\$this.family")
            )
        )
    )
    val c2 = Column(
        "name", "Patient.name", ExplodeWide(
            discriminator = "%index",
            subcolumns = arrayOf(
                Column("", "\$this.given"),
                Column("family", "\$this.family")
            )
        )
    )
    val c1 = Column("name", "Patient.name.given", ExplodeLong(emptyArray()))
    val c4 = Column("test", "@2021-01-01", Singleton)
    val c6 = Column("test2", "12", Singleton)
    val c5 = Column("gender", "Patient.gender", Singleton)

//    rt.addColumn(c, bundle.entry[0].resource, fpe)

    val subtables = mutableListOf<SubTable>()

    for (entryComponent in bundle.entry) {
        val st = SubTable()
        st.addColumn(c2, entryComponent.resource, fpe)
        st.addColumn(c1, entryComponent.resource, fpe)
        st.addColumn(c4, entryComponent.resource, fpe)
        st.addColumn(c5, entryComponent.resource, fpe)
        st.addColumn(c6, entryComponent.resource, fpe)
        println(st)
        subtables += st
    }

    ResultTable(subtables).print(CSVFormat.EXCEL.printer())

}


class SubTable() {
    val data = LinkedHashMap<Pair<Int, String>, List<String?>>()
    val dataType = object : HashMap<String, MutableSet<RDataType>>() {
        override fun get(key: String): MutableSet<RDataType>? {
            return if (super.containsKey(key)) {
                super.get(key)
            } else {
                val default = hashSetOf<RDataType>()
                this[key] = default
                default
            }
        }
    }
    var currentLength = 1
    var colIdx = 1

    fun addColumn(column: Column, base: IBase, fpe: FhirPathEngineWrapper) {
        fun String.expr() = fpe.parseExpression(this)

        val eval = fpe.evaluateToBase(base, column.expression.expr())
        when (val type = column.type) {
            is Singleton -> {
                val value = eval.single() //TODO: Leere Liste erlaubt?
                this.data[colIdx to column.name] = List(currentLength) { fpe.convertToString(value) }
                this.dataType[column.name]?.add(parseFhirType(value)) //TODO: Add singleton to GUI
            }
            is Join -> {
                val value = eval.joinToString(type.delimiter) { fpe.convertToString(it) }
                this.data[colIdx to column.name] = List(currentLength) { value }
                this.dataType[column.name]?.add(if (eval.size == 1) parseFhirType(eval[0]) else RDataType.CHARACTER) //TODO: This means if using join type but no element has multiple entries, we get the type of single element, which might lead to issues when dealing with changing data
            }
            is ExplodeLong -> {
                val sc = if (type.subcolumns.isEmpty()) {
                    listOf(column.name to fpe.parseExpression("\$this"))
                } else {
                    type.subcolumns.map { column.name + (if (column.name.isNotEmpty()) "." else "") + it.name to it.expression.expr() }
                }

                when {
                    eval.size == 1 -> {
                        for ((name, expression) in sc) {
                            //TODO: Handle multiple return values: Throw error?
                            val subitem = fpe.evaluateToBase(eval[0], expression)
                            this.data[colIdx to name] = List(currentLength) {
                                if (subitem.size != 1) null else fpe.convertToString(subitem.first())
                            }
                            if (subitem.size == 1) {
                                this.dataType[name]?.add(parseFhirType(subitem.single()))
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
                        for ((name, expression) in sc) {
                            val result = eval.map { fpe.evaluateToBase(it, expression).singleOrNull() }
                            //TODO Handle multi-value results
                            val resultString = result.map { if (it != null) fpe.convertToString(it) else null }
                            this.data[colIdx to name] = stretchList(resultString, currentLength)
                            this.dataType[name]?.addAll(result.filterNotNull().map { parseFhirType(it) })
                        }
                        this.currentLength = currentLength * eval.size
                    }
                }
            }
            is ExplodeWide -> {
                for ((index, iBase) in eval.withIndex()) {
                    val disc =
                        fpe.evaluateToBase(iBase, type.discriminator.expr(), mapOf("index" to index))
                            .firstOrNull()?.let { fpe.convertToString(it) } ?: "empty"
                    val sc = if (type.subcolumns.isNotEmpty()) {
                        type.subcolumns.map { column.name + (if (column.name.isNotBlank()) "." else "") + disc + (if (it.name != "") ".${it.name}" else "") to it.expression.expr() }
                    } else {
                        listOf(column.name + (if (column.name.isNotBlank()) "." else "") + disc to fpe.parseExpression("\$this"))
                    }
                    for ((name, expression) in sc) {
                        val resultBase = fpe.evaluateToBase(iBase, expression, mapOf("index" to index))
                        val resultString: String? = resultBase.map { fpe.convertToString(it) }.singleOrNull()
                        this.data[colIdx to name] = List(currentLength) { resultString }
                        resultBase.singleOrNull()?.let { parseFhirType(it) }?.let { this.dataType[name]?.add(it) }
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
        for ((r, value) in this.data.entries) {
            val columnName = r.second
            maxLength[columnName] = columnName.length.coerceAtLeast(value.maxOf { it?.length ?: "null".length })
        }
        return buildString {
            append("RDataTypes = ").append(dataType.toString()).append("\n\n")
            for ((_, columnName) in data.keys) {
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

    fun getDataTypes(): Map<String, RDataType> {
        return this.getAllColumnNames()
            .map { (_, name) ->
                Pair(
                    name,
                    subtables.flatMap { it.dataType[name] ?: emptySet() }.toSet().singleOrNull() ?: RDataType.CHARACTER
                )
            }.toMap()
    }

}