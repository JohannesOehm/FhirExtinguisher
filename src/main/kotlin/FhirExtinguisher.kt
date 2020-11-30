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
import org.hl7.fhir.instance.model.api.IBaseResource
import wrappers.BundleEntryComponentWrapper
import wrappers.BundleWrapper
import wrappers.FhirPathEngineWrapperR4
import wrappers.FhirPathEngineWrapperSTU3
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

class FhirExtinguisher(
    private val fhirServerUrl: String,
    private val fhirContext: FhirContext,
    private val interceptors: List<IClientInterceptor>
) {

    val fhirClient = fhirContext.newRestfulGenericClient(fhirServerUrl)

    val fhirPathEngine = if (fhirContext.version.version == FhirVersionEnum.DSTU3) {
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

    /**
     * Process a bundle resource which is submitted with the users call
     */
    suspend fun processBundle(call: ApplicationCall) {
        val jsonParser = fhirContext.newJsonParser()
        val sb = StringBuilder()
        val contentType = call.request.headers["Content-Type"]

        val myParams: MyParams
        val resourceType: String
        val resourceString: String
        if (call.request.contentType().contentType == "multipart" && call.request.contentType().contentSubtype == "form-data") {
            val receiveParameters = call.receiveParameters()
            myParams = processQueryParams(receiveParameters).second
            resourceType = receiveParameters["bundleFormat"] ?: throw Exception("bundleFormat parameter must be set!")
            resourceString = receiveParameters["bundle"] ?: throw Exception("bundle must be set!")
        } else {
            myParams = processQueryParams(call.parameters).second
            resourceType =
                contentType ?: throw Exception("Content-Type header must be set and either xml, json or formData!")
            resourceString = call.receiveText()
        }

        val resource = if (resourceType == "application/json") {
            jsonParser.parseResource(resourceString)
        } else {
            fhirContext.newXmlParser().parseResource(resourceString)
        }

        //TODO: Abort when user cancels request
        val printer = CSVPrinter(sb, myParams.csvFormat)

        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)
        val resultTables = mutableListOf<SubTable>()
        for (bundleEntry in bundleWrapper.entry) {
            resultTables += processBundleEntry(
                myParams.columns!!,
                bundleEntry,
                jsonParser.encodeResourceToString(bundleEntry.resource as IBaseResource)
            )
        }
        ResultTable(resultTables).print(printer)
        call.respondText(sb.toString()) //TODO: Streamify this
    }

    /**
     * Redirect request to the FHIR server and fetch bundles from there
     */
    suspend fun processUrl(call: ApplicationCall) {
        val bundleUrl = URI(call.request.uri.substringAfter("/fhir/")).path

        val (fhirParams, myParams) = if (call.request.httpMethod == HttpMethod.Post) {
            processQueryParams(call.receiveParameters())
        } else {
            processQueryParams(call.parameters)
        }

        val sb = StringBuilder()
        val printer = CSVPrinter(sb, myParams.csvFormat)
        try {
            if (myParams.columns != null) {
                processWithColumns(bundleUrl, fhirParams, myParams, printer, myParams.columns)
            }
        } catch (e: Exception) {
            log.error(e) { "An error occured while serving $bundleUrl!" }
            return call.respond(
                HttpStatusCode.InternalServerError,
                "Error: " + e.message
            )
        }

        call.response.header(
            HttpHeaders.ContentDisposition, ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName, "\"${defaultCsvFileName(bundleUrl, fhirParams)}.csv\""
            ).toString()
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

//        printer.printRecord(columns.map { it.name })

        var count = 0
        var nextUrl: String? = "$uri?$fhirParams"

        val bundleDefintion = fhirContext.getResourceDefinition("Bundle")
        val bundleClass = bundleDefintion.implementingClass

        val subtables = mutableListOf<SubTable>()

        myloop@ do {
            log.info { "Loading Bundle from $nextUrl" }
            val bundle = fhirClient.fetchResourceFromUrl(bundleClass, nextUrl)
            val bundleWrapper = BundleWrapper(bundleDefintion, bundle)
            nextUrl = bundleWrapper.link.find { it.relation == "next" }?.url
            for (bundleEntry in bundleWrapper.entry) {
                subtables += processBundleEntry(columns, bundleEntry)
                count++;
                if (myParams.limit != null && count >= myParams.limit) {
                    break@myloop;
                }
            }
        } while (nextUrl != null)

        ResultTable(subtables).print(printer)

    }


    private fun processBundleEntry(
        columns: List<Column>,
        bundleEntry: BundleEntryComponentWrapper,
        addRaw: String? = null
    ): SubTable {
        val table = SubTable()
        if (addRaw != null) {
            table.addColumn("\$raw", addRaw)
        }

        for (column in columns) {
            try {
                table.addColumn(column, bundleEntry.resource!!, fhirPathEngine)
            } catch (e: Exception) {
                table.addColumn(column.name, e.message ?: "ERROR")
            }
        }
        return table
//        table.print(printer)
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
        val columnsStr = map["__columns"]?.get(0)
        val columns = if (columnsStr != null) columnsParser.parseString(columnsStr) else null

        return MyParams(csvFormat, limit, columns)
    }

}