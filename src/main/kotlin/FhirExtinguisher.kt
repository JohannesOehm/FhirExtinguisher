import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Enumeration
import org.hl7.fhir.r4.utils.FHIRPathEngine
import java.net.URLDecoder
import java.util.*

private val log = KotlinLogging.logger {}

class FhirExtinguisher(
    portname: Int,
    private val fhirServerUrl: String,
    private val fhirContext: FhirContext,
    private val interceptors: List<IClientInterceptor>
) : NanoHTTPD(portname) {

    private val fhirPathEngine = FHIRPathEngine(SimpleWorkerContext())
    val fhirClient = fhirContext.newRestfulGenericClient(fhirServerUrl)

    init {

        fhirPathEngine.hostServices = object : FHIRPathEngine.IEvaluationContext {
            override fun resolveFunction(functionName: String?): FHIRPathEngine.IEvaluationContext.FunctionDetails {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean): Base {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun checkFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<TypeDetails>?
            ): TypeDetails {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun resolveReference(appContext: Any?, url: String): Base? {
                log.info { "Resolving reference $url" }
                return try {
                    val type = url.split("/", limit = 2)[0] //TODO: There must be a better way to do this
                    val requiredClass: Class<IBaseResource> =
                        fhirContext.getResourceDefinition(type).implementingClass as Class<IBaseResource>
                    fhirClient.fetchResourceFromUrl(requiredClass, url) as Base
                } catch (e: Exception) {
                    log.error(e) { "Cannot resolve reference $url!" }
                    null
                }
            }

            override fun conformsToProfile(appContext: Any?, item: Base?, url: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun resolveValueSet(appContext: Any?, url: String?): ValueSet {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun executeFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<MutableList<Base>>?
            ): MutableList<Base> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

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
            val printer = CSVPrinter(sb, CSVFormat.EXCEL)

            if (myParams.columns != null) {
                processWithColumns(uri, fhirParams, myParams, printer, myParams.columns)
            }

            //TODO: Abort when user cancels request
            return newChunkedResponse(Response.Status.OK, "text/csv", sb.toString().byteInputStream())
        } catch (e: Exception) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.message)
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


        interceptors.forEach { fhirClient.registerInterceptor(it) }

        var count = 0
        var nextUrl: String? = "$uri?$fhirParams"

        myloop@ do {
            log.debug { "Loading Bundle from $nextUrl" }
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
                        is Quantity -> ("${it.value} ${it.unit}")
                        is DecimalType -> Objects.toString(it.value)
                        is CodeableConcept -> if (it.text != null) it.text else it.coding.joinToString { it.code }
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