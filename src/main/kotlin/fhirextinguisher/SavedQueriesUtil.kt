package fhirextinguisher

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.File
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


class QueryStorage(val savedQueriesFile: File) {
    //    val savedQueriesFile = File("storedQueries.csv")
    val savedQueries: MutableList<StoredQuery> =
        if (savedQueriesFile.exists()) deserialize(savedQueriesFile.readText()) else mutableListOf()

    fun hasQuery(queryName: String) = savedQueries.any { it.name == queryName }

    fun storeQuery(queryName: String, url: String) {
        savedQueries.removeIf { it.name == queryName }
        savedQueries += StoredQuery(queryName, url)
        save()
    }

    fun deleteQuery(queryName: String) {
        savedQueries.removeIf { it.name == queryName }
        save()
    }

    private fun save() {
        savedQueriesFile.writeText(serialize(savedQueries))
    }

}


fun Routing.savedQueries(path: String, savedQueriesFile: File) {
    val queryStorage = QueryStorage(savedQueriesFile)
    route(path) {
        get {
            call.respondText(
                serialize(queryStorage.savedQueries),
                contentType = ContentType.Text.CSV,
                status = HttpStatusCode.OK
            )
        }
        post("/{name}") {
            val queryName = call.parameters["name"]
            val force = call.parameters["force"] == "true"
            if (queryName != null) {
                if (!force && queryStorage.hasQuery(queryName)) {
                    call.respond(HttpStatusCode.Conflict, "Query name already in use!")
                } else {
                    queryStorage.storeQuery(queryName, url = call.receiveText())
                }
            }
        }
        delete("/{name}") {
            val queryName = call.parameters["name"]
            if (queryName != null) {
                if (queryStorage.hasQuery(queryName)) {
                    queryStorage.deleteQuery(queryName)
                    call.respond("Deleted!")
                } else {
                    call.respond("Cannot find any query with name '$queryName'!")
                }
            }
        }
    }
}