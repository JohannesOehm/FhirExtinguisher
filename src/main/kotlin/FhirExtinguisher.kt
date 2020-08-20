import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import wrappers.BundleEntryComponentWrapper
import wrappers.BundleWrapper
import wrappers.FhirPathEngineWrapperR4
import wrappers.FhirPathEngineWrapperSTU3
import java.net.URI
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


    suspend fun processBundle(call: ApplicationCall) {
        val sb = StringBuilder()
        val (fhirParams, myParams) = processQueryParams(call.parameters) //TODO: Only parse my_params
//        log.debug { "uri = '${call.uri}'; queryParams = ${session.queryParameterString}" }
        log.debug { "fhirParams = $fhirParams, myParams = $myParams" }
        //TODO: Abort when user cancels request
        val printer = CSVPrinter(sb, myParams.csvFormat)


        val resource = if (call.request.headers["Content-Type"] == "application/json") {
            fhirContext.newJsonParser().parseResource(call.receiveStream())
        } else {
            fhirContext.newXmlParser().parseResource(call.receiveStream())
        }

        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)
        printer.printRecord(myParams.columns!!.map { it.name })
        for (bundleEntry in bundleWrapper.entry) {
            processBundleEntry(myParams.columns, bundleEntry, printer)
        }

        call.respondText(sb.toString()) //TODO: Streamify this
    }

    suspend fun processUrl(call: ApplicationCall) {
        println("call.request.uri= " + call.request.uri)
        val bundleUrl = URI(call.request.uri.removePrefix("/fhir/")).path
        val (fhirParams, myParams) = processQueryParams(call.parameters)

        val sb = StringBuilder()
        val printer = CSVPrinter(sb, myParams.csvFormat)
        try {
            if (myParams.columns != null) {
                processWithColumns(bundleUrl, fhirParams, myParams, printer, myParams.columns)
            }
        } catch (e: Exception) {
            log.error(e) { "An error occured while serving $bundleUrl !" }
            return call.respond(
                HttpStatusCode.InternalServerError,
                "Error: " + e.message
            )
        }

        call.response.header(
            "Content-Disposition",
            "attachment; filename=\"${defaultCsvFileName(bundleUrl, fhirParams)}.csv\"');"
        )


        call.respondText(text = sb.toString(), contentType = ContentType.Text.CSV)

    }

    private fun defaultCsvFileName(bundleUrl: String, fhirParams: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        return (bundleUrl + "_" + fhirParams + "_" + timestamp).replace(Regex("[\\\\/$:?<>|\"'*]"), "_")
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

    private fun processQueryParams(parameters: Parameters): Pair<String, MyParams> {
        val (myParams, passThruParams) = parameters.entries()
            .partition {
                listOf("__csvFormat", "__limit", "__columns").any { prefix -> it.key.startsWith(prefix) }
            }
        val fhirParams = passThruParams.flatMap { (key, value) -> value.map { "$key=$it" } }
            .joinToString("&")
        return fhirParams to parseMyParams(myParams)
    }


    private fun parseMyParams(stringsToParse: List<Map.Entry<String, List<String>>>): MyParams {
        val map = stringsToParse.map { it.key to it.value }.toMap()

        val csvFormat = map["__csvFormat"]?.let {
            try {
                CSVFormat.valueOf(it[0])
            } catch (e: Exception) {
                val supported = CSVFormat.Predefined.values().joinToString(", ")
                throw RuntimeException("Unknown CSV Format '$it', supported values are: $supported", e)
            }
        } ?: CSVFormat.EXCEL
        val limit = map["__limit"]?.get(0)?.toInt()
        val columnsStr = URLDecoder.decode(map["__columns"]?.get(0))
        val columns = if (columnsStr != null) columnsParser.parseString(columnsStr) else null

        return MyParams(csvFormat, limit, columns)
    }

}