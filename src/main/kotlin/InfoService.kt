import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class InfoService(private val instanceConfiguration: InstanceConfiguration) {
    fun serve(req: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val result = """{
            "server": "${instanceConfiguration.fhirServerUrl}",
            "version": "${instanceConfiguration.fhirVersion.version.version.fhirVersionString}"
        }"""
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", result)
    }
}