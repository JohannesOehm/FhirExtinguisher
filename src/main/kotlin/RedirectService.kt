import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayOutputStream
import java.io.InputStream

private val log = KotlinLogging.logger {}

class RedirectService(private val urlPrefix: String) {
    fun serve(req: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val client = HttpClients.createDefault()
        val targetUrl = urlPrefix + "/" + req.uri.drop("/redirect/".length) + "?" + req.queryParameterString

        log.info { "Redirecting request to targetUrl = $targetUrl" }

        val response = client.execute(HttpGet(targetUrl))

        val contentType = response.getFirstHeader("Content-Type").value
        val status = convertStatus(response.statusLine)
        val content = inputStreamToString(response.entity.content)
        response.close()

        return NanoHTTPD.newFixedLengthResponse(status, contentType, content)
    }

    private fun convertStatus(statusLine: StatusLine): NanoHTTPD.Response.IStatus {
        val statusCode = statusLine.statusCode
        val reasonPhrase = statusLine.reasonPhrase
        return NanoHTTPD.Response.Status.values().find { it.requestStatus == statusCode } ?: object :
            NanoHTTPD.Response.IStatus {
            override fun getRequestStatus() = statusCode
            override fun getDescription() = reasonPhrase
        }
    }


}

fun inputStreamToString(inputStream: InputStream): String {
    val result = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var length: Int = -1
    while (inputStream.read(buffer).also({ length = it }) != -1) {
        result.write(buffer, 0, length)
    }
    return result.toString("UTF-8")
}