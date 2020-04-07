import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import java.io.PrintWriter
import java.io.StringWriter

private val log = KotlinLogging.logger {}


class MyRouters(portnumber: Int, private val instanceConfiguration: InstanceConfiguration) : NanoHTTPD(portnumber) {

    var fhirExtinguisher = FhirExtinguisher(
        instanceConfiguration.fhirServerUrl,
        instanceConfiguration.fhirVersion,
        instanceConfiguration.interceptors
    )
    var staticPageHandler = StaticPageHandler()

    var redirectService = RedirectService(instanceConfiguration.fhirServerUrl, instanceConfiguration.authData)
    var infoService = InfoService(instanceConfiguration)
    var fhirPathHelper = FhirPathHelperService()

    override fun serve(session: IHTTPSession): Response {
        val clientIp = session.headers["remote-addr"] ?: session.headers["http-client-ip"]
        if (clientIp != "127.0.0.1" && instanceConfiguration.blockExternalRequests) {
            log.info("Request blocked. clientIp = $clientIp")
            return newFixedLengthResponse(
                Response.Status.FORBIDDEN,
                "text/plain",
                "Request blocked because it must come from the localhost (127.0.0.1) IP, but got '$clientIp' instead!"
            )
        }



        try {
            if (session.uri.startsWith("/fhir/")) {
                return fhirExtinguisher.serve(session)
            } else if (session.uri.startsWith("/redirect/")) {
                return redirectService.serve(session)
            } else if (session.uri.startsWith("/info")) {
                return infoService.serve(session)
            } else if (session.uri.startsWith("/fhirpathhelper")) {
                return fhirPathHelper.serve(session)
            } else {
                return staticPageHandler.serve(session)
            }
        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/plain",
                "Server error: " + e.message + "\n\n" + e.getStackTraceAsString()
            )
        }
    }

}

fun Exception.getStackTraceAsString(): String {
    val sw = StringWriter()
    this.printStackTrace(PrintWriter(sw))
    return sw.toString()
}