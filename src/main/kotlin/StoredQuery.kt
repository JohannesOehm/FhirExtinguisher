import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.StringReader

data class StoredQuery(val name: String, val url: String)

val csvFormat: CSVFormat = CSVFormat.EXCEL.withHeader("name", "url")

fun serialize(savedQueries: MutableList<StoredQuery>): String {
    val sb = StringBuilder()
    val printer = CSVPrinter(sb, csvFormat)
    for (savedQuery in savedQueries) {
        printer.printRecord(savedQuery.name, savedQuery.url)
    }
    return sb.toString()
}

fun deserialize(string: String): MutableList<StoredQuery> {
    val result = mutableListOf<StoredQuery>()
    val printer = CSVParser(StringReader(string), csvFormat)
    for (record in printer) {
        result += StoredQuery(record[0], record[1])
    }
    return result
}
