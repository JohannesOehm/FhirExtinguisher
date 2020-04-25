import org.apache.commons.csv.CSVPrinter

class ResultTable() {
    val columns = ArrayList<Column>(0)
    val data = ArrayList<List<String>>()
    var currentLength = 1


    fun addColumn(column: Column, data: List<String>) {
        columns.add(column)

        when (column.listProcessingMode) {
            is Join -> {
                val value = data.joinToString(column.listProcessingMode.delimiter)
                this.data.add(List(currentLength) { value })
            }
            is Explode -> {
                when {
                    data.size == 1 -> {
                        this.data.add(List(currentLength) { data[0] })
                    }
                    data.isEmpty() -> {
                        this.data.add(List(currentLength) { data.toString() })
                    }
                    else -> {
                        for ((i, col) in this.data.withIndex()) {
                            this.data[i] = repeatList(col, data.size)
                        }
                        this.data.add(data)
                        this.currentLength = currentLength * data.size
                    }
                }
            }
            else -> {
                val value = if (data.size == 1) data[0] else data.toString()
                this.data.add(List(currentLength) { value }) // just one element
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
        return list
    }


}