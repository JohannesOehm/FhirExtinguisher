package fhirextinguisher


import io.ktor.server.application.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.body
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.plugins.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.model.Base
import org.hl7.fhir.r4.model.PrimitiveType
import org.slf4j.event.Level
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

private val log = KotlinLogging.logger {}

/**
 * Entry point for WAR deployment
 */
fun Application.application() {
    val myConfig = environment.config.config("FhirExtinguisher")
    val instanceConfiguration = InstanceConfigDTO(
        fhirServerUrl = myConfig.propertyOrNull("fhirServerUrl")?.getString()!!,
        fhirVersion = myConfig.propertyOrNull("fhirVersion")?.getString()!!,
        basicAuth = myConfig.propertyOrNull("authorization")?.getString(),
        tokenAuth = null,
        timeoutInMillis = 60_000,
        blockExternalRequests = false,
        queryStorageFile = myConfig.propertyOrNull("queryStorage")?.getString()!!,
    ).toInstanceConfiguration()
    application2(instanceConfiguration)()
}

fun application2(
    instanceConfiguration: InstanceConfiguration
): Application.() -> Unit = {
    install(CallLogging) {
        level = Level.INFO
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

        redirect(
            "/redirect",
            instanceConfiguration.fhirServerUrl,
            instanceConfiguration.authData,
            instanceConfiguration.timeoutInMillis
        )
        savedQueries("/query-storage", instanceConfiguration.queryStorageFile)
        post("/processBundle") {
            fhirExtinguisher.processBundle(call)
        }
        get("/fhirPath") {
            val expr = call.parameters["expr"]
            if (expr == null) {
                call.respondText("GET-param 'expr' must be set!", status = HttpStatusCode.BadRequest)
            } else {
                try {
                    val exp = fhirExtinguisher.fhirPathEngine.parseExpression(expr)
                    call.respondText("OK", contentType = ContentType.Text.Plain)
                } catch (e: FHIRException) {
                    call.respondText(e.message ?: e.toString(), ContentType.Text.Plain, HttpStatusCode.BadRequest)
                } catch (e: NullPointerException) {
                    call.respondText("Invalid FHIRPath expression!", ContentType.Text.Plain, HttpStatusCode.BadRequest)
                }
            }
        }
        post("/fhirPath") {
            val expr = call.parameters["expr"]
            if (expr == null) {
                call.respondText("GET-param 'expr' must be set!", status = HttpStatusCode.BadRequest)
            } else {
                val jsonParser = instanceConfiguration.fhirVersion.newJsonParser()
                val resource = withContext(Dispatchers.IO) { jsonParser.parseResource(call.receiveStream()) }
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
        route("/fhir/{...}") {
            handle {
                fhirExtinguisher.processUrl(call)
            }
        }
        get("/info") {
            call.respond(buildJsonObject {
                put("server", instanceConfiguration.fhirServerUrl)
                put("version", instanceConfiguration.fhirVersion.version.version.name.lowercase())
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
fun Routing.redirect(prefix: String, target: String, authData: AuthData?, timeoutInMillis: Int) {
    route("$prefix/{...}") {
        redirect(timeoutInMillis, authData, target, prefix)
    }
    route("$prefix/") {
        redirect(timeoutInMillis, authData, target, prefix)
    }
}

private fun Route.redirect(
    timeoutInMillis: Int,
    authData: AuthData?,
    target: String,
    prefix: String
) {
    handle {
        val client = HttpClient(Apache) {
            engine {
                connectTimeout = timeoutInMillis
                socketTimeout = timeoutInMillis
                connectionRequestTimeout = timeoutInMillis
            }
            if (authData != null) {
                install(Auth) {
                    if (authData is BasicAuthData) {
                        basic {
                            sendWithoutRequest { true }
                            credentials { BasicAuthCredentials(authData.username, authData.password) }
                        }
                    } else if (authData is BearerAuthData) {
                        bearer {
                            sendWithoutRequest { true }
                            loadTokens { BearerTokens(authData.token, authData.token) }
                        }
                    }
                }
            }
        }

        val originalUri = call.request.uri

        val redirectUrl = target.dropLastWhile { it == '/' } + "/" + originalUri.substringAfter("$prefix/")
        log.debug { "redirecting '$originalUri' to '$redirectUrl'" }
        try {
            val response = client.request(redirectUrl)
            //TODO: Add Query Parameters
            // Get the relevant headers of the client response.
            val proxiedHeaders = response.headers
            val location = proxiedHeaders[HttpHeaders.Location]
            val contentType = proxiedHeaders[HttpHeaders.ContentType]
            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

            if (location != null) {
                call.response.header(HttpHeaders.Location, location.removePrefix(prefix))
            }
            //TODO: Find a solution that works in tomcat behind AJP reverse proxy but does not block
            val bytes = runBlocking { response.readBytes() }

            proxiedHeaders.filter { key, _ ->
                !key.equals(HttpHeaders.ContentType, ignoreCase = true)
                        && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
                        && !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
            }.forEach { key, value ->
                value.forEach { call.response.header(key, it) }
            }
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
<script>window.process = {env: {NODE_DEBUG: false}} //Hack to resolve problem with npm package "util"</script>
<script src="bundle.js"></script>
</body>
</html>"""


