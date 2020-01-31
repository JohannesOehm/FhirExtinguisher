import ca.uhn.fhir.context.FhirContext
import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Enumeration
import org.hl7.fhir.r4.model.ExpressionNode
import org.hl7.fhir.r4.utils.FHIRPathEngine
import java.net.URLDecoder

private val log = KotlinLogging.logger {}

class FhirExtinguisher(portname: Int, private val fhirServerUrl: String) : NanoHTTPD(portname) {

    val fhirCtxt = FhirContext.forR4()

    private val fhirPathEngine = FHIRPathEngine(SimpleWorkerContext())

    data class MyParams(
        val csvFormat: String,
        val limit: Int?,
        val columns: List<Column>?
    )

    data class Column(
        val name: String,
        val expression: ExpressionNode,
        val listProcessingMode: String
    )

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        log.debug { "uri = ${session.uri}" }
        log.debug { "queryParams = " + session.queryParameterString }

        try {
            val (fhirParams, myParams) = processQueryParams(session)

            log.debug { "fhirParams = $fhirParams, myParams = $myParams" }

            val sb = StringBuilder()
            var printer = CSVPrinter(sb, CSVFormat.EXCEL)

            if (myParams.columns != null) {
                processWithColumns(uri, fhirParams, myParams, printer, myParams.columns)
            }

            //TODO: Abort when user cancels request
            return newChunkedResponse(Response.Status.OK, "text/csv", sb.toString().byteInputStream())
        } catch (e: Exception) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.message)
        }

    }

    private val fhirClient = fhirCtxt.newRestfulGenericClient(fhirServerUrl)

    private fun processWithColumns(
        uri: String?,
        fhirParams: String,
        myParams: MyParams,
        printer: CSVPrinter,
        columns: List<Column>
    ) {

        printer.printRecord(columns.map { it.name })

        var count = 0;
        var nextUrl: String? = "$uri?$fhirParams"

        myloop@ do {
            val bundle = fhirClient.fetchResourceFromUrl(Bundle::class.java, nextUrl)
            nextUrl = bundle.link.find { it.relation == "next" }?.url
            for (bundleEntry in bundle.entry) {
                processResource(columns, bundleEntry, printer)
                count++;
                if (myParams.limit != null && count >= myParams.limit) {
                    break@myloop;
                }
            }
        } while (nextUrl != null)

    }

    private fun processResource(
        columns: List<Column>,
        bundleEntry: Bundle.BundleEntryComponent,
        printer: CSVPrinter
    ) {
        var table = ResultTable()
        for (column in columns) {
            try {
                val result = fhirPathEngine.evaluate(bundleEntry.resource, column.expression)
                table.addColumn(column, result.map {
                    when (it) {
                        is Enumeration<*> -> it.code
                        else -> it.toString()
                    }
                })
            } catch (e: Exception) {
                table.addColumn(column, listOf(e.message ?: "ERROR"))
            }
        }
        table.print(printer)
    }

    private fun processQueryParams(session: IHTTPSession): Pair<String, MyParams> {
        if (session.queryParameterString != null) {
            val (myParams, passThruParams) = session.queryParameterString
                .split('&')
                .partition {
                    listOf("__csvFormat", "__limit", "__columns").any { prefix -> it.startsWith(prefix) }
                }
            return passThruParams.joinToString("&") to parseMyParams(myParams)
        } else {
            return "" to MyParams(",", -1, emptyList())
        }
    }


    private fun parseMyParams(stringsToParse: List<String>): MyParams {
        val map = stringsToParse.map { it.split('=', limit = 2) }
            .map { it[0] to it[1] }
            .toMap()

        val csvFormat = map["__csvFormat"] ?: ","
        val limit = map["__limit"]?.toInt()
        val columnsStr = map["__columns"]
        val columns = if (columnsStr != null) parseColumns(columnsStr) else null

        return MyParams(csvFormat, limit, columns)
    }

    private fun parseColumns(stringToParse: String): List<Column> {
        return stringToParse.split(',')
            .map { it.split(':', limit = 2) }
            .filter { it.size == 2 }
            .map {
                val splitted = URLDecoder.decode(it[0]).split('@', limit = 2);
                val listProcessingMode = if (splitted.size == 2) splitted[1] else "flatten"
                val expressionStr = URLDecoder.decode(it[1])

                val expression = try {
                    fhirPathEngine.parse(expressionStr) //TODO: Thread-Safe?
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

                Column(splitted[0], expression, listProcessingMode)
            }
    }
}