package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.hl7.fhir.exceptions.FHIRException
import org.slf4j.event.Level
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


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
    val fhirServerUrl: String? = cmd.getOptionValue("fhirServer")?.dropLastWhile { it == '/' }
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


    val instanceConfiguration = createInstanceConfiguration(
        fhirVersion,
        authorization,
        fhirServerUrl,
        external
    )


    embeddedServer(Netty, portnumber, module = application2(instanceConfiguration)).start(wait = true)
}

fun Application.application() {
    val myConfig = environment.config.config("FhirExtinguisher")
    val instanceConfiguration = createInstanceConfiguration(
        fhirServerUrl = myConfig.propertyOrNull("fhirServerUrl")?.getString()!!,
        fhirVersion = myConfig.propertyOrNull("fhirVersion")?.getString()!!,
        authorization = myConfig.propertyOrNull("authorization")?.getString(),
        external = true
    )
    application2(instanceConfiguration)()
}

private fun application2(
    instanceConfiguration: InstanceConfiguration
): Application.() -> Unit = {
    install(CallLogging) {
        level = Level.DEBUG
    }

    val fhirExtinguisher = FhirExtinguisher(
        instanceConfiguration.fhirServerUrl,
        instanceConfiguration.fhirVersion,
        instanceConfiguration.interceptors
    )
    routing {
        intercept(ApplicationCallPipeline.Features) {
            val ip = call.request.local.remoteHost
            if (instanceConfiguration.blockExternalRequests && !isThisMyIpAddress(InetAddress.getByName(ip))) {
                call.respondText("The request origin '$ip' is not a localhost address. Please start the FhirExtinguisher with '-ext'!")
                this.finish()
            }
        }

        redirect("/redirect", instanceConfiguration.fhirServerUrl)
        savedQueries("/query-storage")
        post("/processBundle") {
            fhirExtinguisher.processBundle(call)
        }
        post("/fhirPath") {
            val expr = call.parameters["expr"]
            if (expr == null) {
                call.respondText("GET-param 'expr' must be set!", status = HttpStatusCode.BadRequest)
            } else {
                val resource = instanceConfiguration.fhirVersion.newJsonParser().parseResource(call.receiveStream())
                val expressionWrapper = try {
                    fhirExtinguisher.fhirPathEngine.parseExpression(expr)
                } catch (e: FHIRException) {
                    call.respondText(e.message ?: e.toString(), ContentType.Text.Plain, HttpStatusCode.BadRequest)
                    return@post
                }
                val results = fhirExtinguisher.fhirPathEngine.evaluateToBase(resource, expressionWrapper)
                val json = buildJsonArray {
                    for (result in results) {
                        add(fhirExtinguisher.fhirPathEngine.convertToString(result))
                    }
                }
                call.respondText(json.toString(), contentType = ContentType.Application.Json)
            }
        }
        route("/fhir/*") {
            handle {
                fhirExtinguisher.processUrl(call)
            }
        }
        get("/info") {
            call.respond(buildJsonObject {
                put("server", instanceConfiguration.fhirServerUrl)
                put("version", instanceConfiguration.fhirVersion.version.version.fhirVersionString)
            }.toString())
        }
        get("/") {
            call.respondText(index_html(context.request.uri), contentType = ContentType.Text.Html)
        }
        static("/") {
            resources("static")
//            defaultResource("index.html", "static")
        }
    }
}

private fun createInstanceConfiguration(
    fhirVersion: String?,
    authorization: String?,
    fhirServerUrl: String,
    external: Boolean
): InstanceConfiguration {
    val fhirContext = when (fhirVersion) {
        "stu3", "dstu3" -> FhirContext.forDstu3()
        else -> FhirContext.forR4()
    }

    val (interceptors, basicAuth) = if (authorization != null) {
        val (username, passwd) = authorization.split(':', limit = 2)
        listOf(BasicAuthInterceptor(username, passwd)) to BasicAuthData(username, passwd)
    } else emptyList<IClientInterceptor>() to null


    //TODO: Validate FHIR server URL

    return InstanceConfiguration(fhirServerUrl, fhirContext, basicAuth, interceptors, !external)
}

fun isThisMyIpAddress(addr: InetAddress): Boolean {
    // Check if the address is a valid special local or loop back
    return if (addr.isAnyLocalAddress || addr.isLoopbackAddress) true else try {
        // Check if the address is defined on any interface
        NetworkInterface.getByInetAddress(addr) != null
    } catch (e: SocketException) {
        false
    }
}


/**
 * Reverse proxy method
 */
fun Routing.redirect(prefix: String, target: String) {
    route("$prefix/{...}") {
        handle {
            val client = HttpClient()

            val originalUri = call.request.uri

            val redirectUrl = target.dropLastWhile { it == '/' } + "/" + originalUri.substringAfter("$prefix/")
            log.debug { "redirecting to $redirectUrl" }
            try {
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
                //TODO: Find a solution that works in tomcat behind AJP reverse proxy but does not block
                val bytes = runBlocking { response.content.toByteArray() }
                log.info { "Received bytes $bytes" }

//                proxiedHeaders.filter { key, value ->  key.equals(HttpHeaders.ContentType, ignoreCase = true)
//                                   && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
//                                   && !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)}.forEach { key, value ->
//                    call.response.header(key, value)
//                }
                call.respondBytes(
                    bytes = bytes,
                    contentType = contentType?.let { ContentType.parse(it) },
                    status = response.status
                )


                // In the case of other content, we simply pipe it. We return a [OutgoingContent.WriteChannelContent]
                // propagating the contentLength, the contentType and other headers, and simply we copy
                // the ByteReadChannel from the HTTP client response, to the HTTP server ByteWriteChannel response.
//                call.respond(object : OutgoingContent.ByteArrayContent() {
//                    override val contentLength: Long? = contentLength?.toLong()
//                    override val contentType: ContentType? =
//                    override val headers: Headers = Headers.build {
//                        appendAll(proxiedHeaders.filter { key, _ ->
//                            !key.equals(HttpHeaders.ContentType, ignoreCase = true)
//                                    && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
//                                    && !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
//                        })
//                    }
//                    override val status: HttpStatusCode = response.status
//                    override fun bytes(): ByteArray {
//                        return bytes
//                    }
//                })
            } catch (e: ServerResponseException) {
                call.respondText(
                    "Request got successfully redirected to '$redirectUrl', but server responded with '${e.response.status}'!\n\n${e.message}",
                    status = HttpStatusCode.InternalServerError
                )
            } catch (e: Exception) {
                call.respondText(
                    "FhirExtinguisher/Ktor-Client-Exception: Cannot redirect to '$redirectUrl'!\n \n${e.stackTraceToString()}",
                    status = HttpStatusCode.InternalServerError
                )
            }
            client.close()
        }
    }


}

fun index_html(baseHref: String = "/") = """<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0"
          name="viewport">
    <meta content="ie=edge" http-equiv="X-UA-Compatible">
    <title>FhirExtinguisher</title>
    <link href="styles.css" rel="stylesheet"/>
    <link rel="icon" href="favicon.png" type="image/png">
    <base href="$baseHref" />
</head>
<body>
<div id="app"></div>
<script src="bundle.js"></script>
</body>
</html>"""

