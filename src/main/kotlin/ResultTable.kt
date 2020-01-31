import org.apache.commons.csv.CSVPrinter

class ResultTable() {
    val columns = ArrayList<FhirExtinguisher.Column>(0)
    val data = ArrayList<List<String>>()
    var currentLength = 1


    fun addColumn(column: FhirExtinguisher.Column, data: List<String>) {
        columns.add(column)

        if (column.listProcessingMode == "flatten") {
            val value = data.toString();
            this.data.add(List(currentLength) { value }) // just one element

        } else if (column.listProcessingMode.startsWith("join")) {
            val regex = "join\\(\\s*\"([^\"])*\"\\s*\\)".toRegex()
            if (!regex.matches(column.listProcessingMode)) {
                throw RuntimeException("Invalid @join expression at column '${column.name}': ${column.listProcessingMode}")
            }
            val separator = regex.find(column.listProcessingMode)!!.groupValues[1]
            this.data.add(listOf(data.joinToString(separator)))
        } else if (column.listProcessingMode == "explode") {
            if (data.size == 1) {
                this.data.add(List(currentLength) { data.toString() })
            } else if (data.isEmpty()) {
                this.data.add(List(currentLength) { data.toString() })
            } else {
                for ((i, col) in this.data.withIndex()) {
                    this.data[i] = repeatList(col, data.size)
                }
                this.data.add(data)
                this.currentLength = currentLength * data.size
            }

        }
    }

    fun print(printer: CSVPrinter) {
        for (i in 0 until currentLength) {
            for (data in this.data) {
                printer.print(data[i])
            }
            printer.println()
        }
    }

    private fun repeatList(col: List<String>, times: Int): List<String> {
        val list = ArrayList<String>(col.size * times)
        repeat(times) {
            list.addAll(col)
        }
        return list;
    }


}