import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class FhirPathHelperService {
    fun serve(req: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (req.uri.contains("/validate")) {

        }
        return NanoHTTPD.newFixedLengthResponse("not implemented yet!")
    }
}