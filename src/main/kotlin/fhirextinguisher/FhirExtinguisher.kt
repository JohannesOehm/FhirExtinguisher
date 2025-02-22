package fhirextinguisher

import Column
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.Parameters
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.*
import parseColumns
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


    init {
        interceptors.forEach { fhirClient.registerInterceptor(it) }
    }

    data class MyParams(
        val csvFormat: CSVFormat,
        val limit: Int?,
        val columns: List<Column>?
    )

    @Serializable
    data class PostParams(
        val limit: Int,
        val columns: String,
        val bundle: String,
        val bundleFormat: String
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
        if (call.request.contentType().contentType == "application" && call.request.contentType().contentSubtype == "columns+json") {
            val postParams: PostParams = Json.decodeFromString(call.receive())
            myParams = MyParams(CSVFormat.EXCEL, postParams.limit, parseColumns(postParams.columns).toList())
            resourceType = postParams.bundleFormat
            resourceString = postParams.bundle
        } else {
            myParams = processQueryParams(call.parameters).second
            if (contentType == null) {
                log.info { "Content-Type is null" }
                throw Exception("Content-Type header must be set and either xml, json or formData!")
            }
            resourceType = contentType
            resourceString = call.receiveText()
        }

        val resource = if (ContentType.parse(resourceType).contentSubtype == "json") {
            jsonParser.parseResource(resourceString)
        } else {
            fhirContext.newXmlParser().parseResource(resourceString)
        }

        log.debug { "Received bundle to process with params = $myParams" }

        //TODO: Abort when user cancels request
        val printer = CSVPrinter(sb, myParams.csvFormat)

        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)
        cacheBundleReference(bundleWrapper, myParams.columns!!)
        val resultTables = mutableListOf<SubTable>()
        for (bundleEntry in bundleWrapper.entry) {
            resultTables += processBundleEntry(
                myParams.columns,
                bundleEntry,
                jsonParser.encodeResourceToString(bundleEntry.resource as IBaseResource)
            )
        }
        val resultTable = ResultTable(resultTables)
        call.response.header(
            "R-DataTypes",
            encodeToString(MapSerializer(String.serializer(), RDataType.serializer()), resultTable.getDataTypes())
        )
        resultTable.print(printer)
        val text = sb.toString()
        call.respondText(text) //TODO: Streamify this
        fhirPathEngine.clearCache()
    }

    /**
     * Redirect request to the FHIR server and fetch bundles from there
     */
    suspend fun processUrl(call: ApplicationCall) {
        fhirPathEngine.clearCache()
        val bundleUrl = Url(call.request.uri.substringAfter("/fhir/")).encodedPath

        val (fhirParams, myParams) = if (call.request.httpMethod == HttpMethod.Post) {
            processQueryParams(call.receiveParameters())
        } else {
            processQueryParams(call.parameters)
        }
        if (myParams.columns == null) {
            call.respond(HttpStatusCode.BadGateway, "Please set __columns parameter!")
            return
        }


        try {

            val resultTable = processWithColumns(bundleUrl, fhirParams, myParams.limit, myParams.columns)
            call.response.header(
                HttpHeaders.ContentDisposition, attachment("${defaultCsvFileName(bundleUrl, fhirParams)}.csv")
            )
            call.response.header(
                "R-DataTypes",
                encodeToString(MapSerializer(String.serializer(), RDataType.serializer()), resultTable.getDataTypes())
            )

            val sb = StringBuilder()
            val printer = CSVPrinter(sb, myParams.csvFormat)
            resultTable.print(printer)
            call.respondText(text = sb.toString(), contentType = ContentType.Text.CSV)
        } catch (e: Exception) {
            log.error(e) { "An error occured while serving $bundleUrl!" }
            fhirPathEngine.clearCache()
            return call.respond(
                HttpStatusCode.InternalServerError,
                "Error: " + e.message
            )
        }
    }

    private fun attachment(filename: String) = ContentDisposition.Attachment.withParameter(
        ContentDisposition.Parameters.FileName, filename
    ).toString()

    private fun defaultCsvFileName(bundleUrl: String, fhirParams: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        return (bundleUrl + "_" + fhirParams + "_" + timestamp).replace(Regex("[\\\\/$:?<>|\"'*]"), "_")
    }

    private fun processWithColumns(
        uri: String?,
        fhirParams: String,
        limit: Int?,
        columns: List<Column>
    ): ResultTable {
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
            cacheBundleReference(bundleWrapper, columns)
            for (bundleEntry in bundleWrapper.entry) {
                subtables += processBundleEntry(columns, bundleEntry)
                count++
                if (limit != null && count >= limit) {
                    break@myloop;
                }
            }
        } while (nextUrl != null)

        return ResultTable(subtables)
    }

    private fun cacheBundleReference(bundle: BundleWrapper, columns: List<Column>) {
        try {
            if (fhirPathEngine !is FhirPathEngineWrapperR4) return
            val columns2 = columns.filter { it.expression.contains("resolve") }

            val references = mutableListOf<String>()
            for (entry in bundle.entry) {
                for (column in columns2) {
                    references.addAll(fhirPathEngine.extractReference(entry.resource as Base, column.expression))
                }
            }
            val referencesByType = references.map { IdType(it) }.groupBy { it.resourceType }
            for ((type, references) in referencesByType) {
                val ids = references.map { it.idPart }.distinct()
                log.info { "Resolving $type $ids from server!" }
                val bundle2 =
                    fhirClient.search<Bundle>().forResource(type).where(BaseResource.RES_ID.exactly().codes(ids))
                        .count(1000).execute()
                bundle2.entry.map { it.resource }
                    .forEach { fhirPathEngine.addCacheEntry(type + "/" + it.idElement.idPart, it) }
            }
        } catch (e: Exception) {
            log.debug(e) { "Cannot pre-fetch resources" }
        }

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

        val fhirParams = Parameters.build {
            for (passThruParam in passThruParams) {
                appendAll(passThruParam.key, passThruParam.value)
            }
        }.formUrlEncode()
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
        val columns = if (columnsStr != null) parseColumns(columnsStr) else null

        return MyParams(csvFormat, limit, columns?.toList())
    }


}