import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newChunkedResponse
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import wrappers.*

import java.net.URLDecoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

class FhirExtinguisher(
    private val fhirServerUrl: String,
    private val fhirContext: FhirContext,
    private val interceptors: List<IClientInterceptor>
) {

    val fhirClient = fhirContext.newRestfulGenericClient(fhirServerUrl)

    private val fhirPathEngine = if (fhirContext.version.version == FhirVersionEnum.DSTU3) {
        FhirPathEngineWrapperSTU3(fhirContext, fhirClient)
    } else {
        FhirPathEngineWrapperR4(fhirContext, fhirClient)
    }

    val columnsParser = ColumnsParser(fhirPathEngine)

    init {
        interceptors.forEach { fhirClient.registerInterceptor(it) }
    }

    data class MyParams(
        val csvFormat: CSVFormat,
        val limit: Int?,
        val columns: List<Column>?
    )


    fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val sb = StringBuilder()
        val (fhirParams, myParams) = processQueryParams(session)
        log.debug { "uri = '${session.uri}'; queryParams = ${session.queryParameterString}" }
        log.debug { "fhirParams = $fhirParams, myParams = $myParams" }
        //TODO: Abort when user cancels request
        val printer = CSVPrinter(sb, myParams.csvFormat)


        if (session.method === NanoHTTPD.Method.POST) {
            val body = getBody(session)
            val resource = fhirContext.newJsonParser().parseResource(body)
            val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
            val bundleWrapper = BundleWrapper(bundleDefinition, resource)
            printer.printRecord(myParams.columns!!.map { it.name })
            for (bundleEntry in bundleWrapper.entry) {
                processBundleEntry(myParams.columns, bundleEntry, printer)
            }
        } else {
            val bundleUrl = session.uri.drop("/fhir/".length)
            try {
                if (myParams.columns != null) {
                    processWithColumns(bundleUrl, fhirParams, myParams, printer, myParams.columns)
                }
            } catch (e: Exception) {
                log.error(e) { "An error occured while serving $bundleUrl !" }
                return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain",
                    "Error: " + e.message
                )
            }
        }

        return newChunkedResponse(NanoHTTPD.Response.Status.OK, "text/csv", sb.toString().byteInputStream()).apply {
            val resourceName = if (session.uri.startsWith("/fhir/")) session.uri.drop("/fhir/".length) else session.uri
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val filename = (resourceName + "_" + fhirParams + "_" + timestamp).replace(Regex("[\\\\/$:?<>|\"'*]"), "_")
            addHeader("Content-Disposition", "attachment; filename=\"$filename.csv\"');")
        }
    }


    private fun processWithColumns(
        uri: String?,
        fhirParams: String,
        myParams: MyParams,
        printer: CSVPrinter,
        columns: List<Column>
    ) {

        printer.printRecord(columns.map { it.name })

        var count = 0
        var nextUrl: String? = "$uri?$fhirParams"

        val bundleDefintion = fhirContext.getResourceDefinition("Bundle")
        val bundleClass = bundleDefintion.implementingClass


        myloop@ do {
            log.debug { "Loading Bundle from $nextUrl" }
            val bundle = fhirClient.fetchResourceFromUrl(bundleClass, nextUrl)
            val bundleWrapper = BundleWrapper(bundleDefintion, bundle)
            nextUrl = bundleWrapper.link.find { it.relation == "next" }?.url
            for (bundleEntry in bundleWrapper.entry) {
                processBundleEntry(columns, bundleEntry, printer)
                count++;
                if (myParams.limit != null && count >= myParams.limit) {
                    break@myloop;
                }
            }
        } while (nextUrl != null)

    }


    private fun processBundleEntry(
        columns: List<Column>,
        bundleEntry: BundleEntryComponentWrapper,
        printer: CSVPrinter
    ) {
        val table = ResultTable()
        for (column in columns) {
            try {
                val result = fhirPathEngine.evaluateToStringList(bundleEntry.resource!!, column.expression)
                table.addColumn(column, result)
            } catch (e: Exception) {
                table.addColumn(column, listOf(e.message ?: "ERROR"))
            }
        }
        table.print(printer)
    }

    private fun processQueryParams(session: NanoHTTPD.IHTTPSession): Pair<String, MyParams> {
        if (session.queryParameterString != null) {
            val (myParams, passThruParams) = session.queryParameterString
                .split('&')
                .partition {
                    listOf("__csvFormat", "__limit", "__columns").any { prefix -> it.startsWith(prefix) }
                }
            return passThruParams.joinToString("&") to parseMyParams(myParams)
        } else {
            return "" to MyParams(CSVFormat.EXCEL, -1, emptyList())
        }
    }


    private fun parseMyParams(stringsToParse: List<String>): MyParams {
        val map = stringsToParse.map { it.split('=', limit = 2) }
            .map { it[0] to it[1] }
            .toMap()

        val csvFormat = if (map["__csvFormat"] != null) {
            val csvFormat = map["__csvFormat"]
            try {
                CSVFormat.valueOf(csvFormat)
            } catch (e: Exception) {
                val supported = CSVFormat.Predefined.values().joinToString(", ")
                val message =
                    "Unknown CSV Format '$csvFormat', supported values are: $supported"
                throw RuntimeException(message, e)
            }
        } else {
            CSVFormat.EXCEL
        }
        val limit = map["__limit"]?.toInt()
        val columnsStr = URLDecoder.decode(map["__columns"])
        val columns = if (columnsStr != null) columnsParser.parseString(columnsStr) else null

        return MyParams(csvFormat, limit, columns)
    }

//    private fun parseColumns(stringToParse: String): List<Column> {
//        return stringToParse.split(',')
//            .map { it.split(':', limit = 2) }
//            .filter { it.size == 2 }
//            .map {
//                val splitted = URLDecoder.decode(it[0]).split('@', limit = 2);
//                val listProcessingMode = if (splitted.size == 2) splitted[1] else "flatten"
//                val expressionStr = URLDecoder.decode(it[1])
//
//                val expression = try {
//                    fhirPathEngine.parseExpression(expressionStr) //TODO: Thread-Safe?
//                } catch (e: Exception) {
//                    throw RuntimeException("Error parsing FHIRPath-Expression: $expressionStr", e)
//                }
//
////                println(expression.toString() + ": " + (expression as ExpressionR4).expression.types)
//
//                Column(splitted[0], expression, listProcessingMode)
//            }
//    }
}