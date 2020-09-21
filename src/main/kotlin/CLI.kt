import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveText
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.filter
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyAndClose
import mu.KotlinLogging
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File

private val log = KotlinLogging.logger {}


fun main(args: Array<String>) {
    val options = Options()
        .addOption("f", "fhirServer", true, "The endpoint URL of the FHIR server to call.")
        .addOption(
            "v",
            "fhirVersion",
            true,
            "The FHIR (D)STU/R version to use. Must be either dstu3 or r4."
        )
        .addOption(
            "a",
            "authorization",
            true,
            "Basic Authorization required to connect to the FHIR server (if any). \"username:password\"!"
        )
        .addOption("p", "portNumber", true, "The port number for this server to use.")
        .addOption(
            "ext",
            "allowExternalConnection",
            false,
            "Allow external (non-localhost) connections (might be a security issue)"
        )

    if (args.isEmpty()) {
        HelpFormatter().printHelp("FhirExtinguisher", options)
        return
    }

    val cmd = DefaultParser().parse(options, args)

    val portnumber: Int? = cmd.getOptionValue("portNumber")?.toInt()
    val fhirServerUrl: String? = cmd.getOptionValue("fhirServer").dropLastWhile { it == '/' }
    val fhirVersion: String? = cmd.getOptionValue("fhirVersion")
    val authorization: String? = cmd.getOptionValue("authorization")
    val external: Boolean = cmd.hasOption("allowExternalConnection")

    if (portnumber == null) {
        println("Please provide a portnumber for the local server to start!")
        return
    }

    if (fhirServerUrl == null) {
        println("Please provide a URL for the FHIR server to connect!")
        return
    }


    val fhirContext = when (fhirVersion) {
        "stu3", "dstu3" -> FhirContext.forDstu3()
        else -> FhirContext.forR4()
    }

    val (interceptors, basicAuth) = if (authorization != null) {
        val (username, passwd) = authorization.split(':', limit = 2)
        listOf(BasicAuthInterceptor(username, passwd)) to BasicAuthData(username, passwd)
    } else emptyList<IClientInterceptor>() to null


    //TODO: Validate FHIR server URL

    val instanceConfiguration =
        InstanceConfiguration(fhirServerUrl, fhirContext, basicAuth, interceptors, !external)

    val savedQueriesFile = File("storedQueries.csv")
    val savedQueries: MutableList<StoredQuery> =
        if (savedQueriesFile.exists()) deserialize(savedQueriesFile.readText()) else mutableListOf()

    val fhirExtinguisher = FhirExtinguisher(fhirServerUrl, fhirContext, interceptors)
    embeddedServer(Netty, 8080) {
        routing {
            redirect("/redirect", fhirServerUrl)
            post("/processBundle") {
                fhirExtinguisher.processBundle(call)
            }
            get("/query") {
                call.respondText(
                    serialize(savedQueries),
                    contentType = ContentType.Text.CSV,
                    status = HttpStatusCode.OK
                )
            }
            post("/query/{name}") {
                val queryName = call.parameters["name"]
                val force = call.parameters["force"] == "true"
                if (queryName != null) {
                    if (!force && savedQueries.any { it.name == queryName }) {
                        call.respond(HttpStatusCode.Conflict, "Query name already in use")
                    } else {
                        if (force) {
                            savedQueries.removeIf { it.name == queryName }
                        }
                        savedQueries += StoredQuery(queryName, call.receiveText())
                    }
                    savedQueriesFile.writeText(serialize(savedQueries))
                }
            }
            delete("/query/{name}") {
                val queryName = call.parameters["name"]
                if (queryName != null) {
                    savedQueries.removeIf { it.name == queryName }
                    savedQueriesFile.writeText(serialize(savedQueries))
                }
            }
            route("/fhir/*") {
                handle {
                    fhirExtinguisher.processUrl(call)
                }
            }
            get("/info") {
                call.respond(
                    """{
                            "server": "${instanceConfiguration.fhirServerUrl}",
                            "version": "${instanceConfiguration.fhirVersion.version.version.fhirVersionString}"
                    }"""
                )
            }
            static("/") {
                resources("static")
                defaultResource("index.html", "static")
            }
        }
    }.start(wait = true)

}


/**
 * Reverse proxy method
 */
private fun Routing.redirect(prefix: String, target: String) {
    route("$prefix/{...}") {
        handle {
            val client = HttpClient()
            val redirectUrl =
                "${target.dropLastWhile { it == '/' }}/${call.request.uri.removePrefix(prefix).removePrefix("/")}"
            log.info { "redirecting to $redirectUrl" }
            val response = client.request<HttpResponse>(redirectUrl)


            //TODO: Add Query Parameters

            // Get the relevant headers of the client response.
            val proxiedHeaders = response.headers
            val location = proxiedHeaders[HttpHeaders.Location]
            val contentType = proxiedHeaders[HttpHeaders.ContentType]
            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

            // Propagates location header, removing the wikipedia domain from it
            if (location != null) {
                call.response.header(HttpHeaders.Location, location.removePrefix(prefix))
            }


            // In the case of other content, we simply pipe it. We return a [OutgoingContent.WriteChannelContent]
            // propagating the contentLength, the contentType and other headers, and simply we copy
            // the ByteReadChannel from the HTTP client response, to the HTTP server ByteWriteChannel response.
            call.respond(object : OutgoingContent.WriteChannelContent() {
                override val contentLength: Long? = contentLength?.toLong()
                override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
                override val headers: Headers = Headers.build {
                    appendAll(proxiedHeaders.filter { key, _ ->
                        !key.equals(HttpHeaders.ContentType, ignoreCase = true)
                                && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
                                && !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
                    })
                }
                override val status: HttpStatusCode? = response.status
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    response.content.copyAndClose(channel)
                }
            })
        }
    }


}
