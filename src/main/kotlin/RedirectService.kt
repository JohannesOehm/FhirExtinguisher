import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayOutputStream
import java.io.InputStream

private val log = KotlinLogging.logger {}

class RedirectService(private val urlPrefix: String) {
    fun serve(req: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val client = HttpClients.createDefault()
        val targetUrl = urlPrefix + "/" + req.uri.drop("/redirect/".length)

        log.info { "targetUrl = $targetUrl" }

        val response = client.execute(HttpGet(targetUrl))
        val myResponse = NanoHTTPD.newFixedLengthResponse(inputStreamToString(response.entity.content))
        response.close()
        return myResponse
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int = -1
        while (inputStream.read(buffer).also({ length = it }) != -1) {
            result.write(buffer, 0, length)
        }
        return result.toString("UTF-8")
    }

}