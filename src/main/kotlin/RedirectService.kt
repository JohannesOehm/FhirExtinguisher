import fi.iki.elonen.NanoHTTPD
import mu.KotlinLogging
import org.apache.http.StatusLine
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import java.io.ByteArrayOutputStream
import java.io.InputStream


private val log = KotlinLogging.logger {}

class RedirectService(private val urlPrefix: String, private val auth: BasicAuthData?) {
    fun serve(req: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val clientBuilder = HttpClientBuilder.create()
        if (auth != null) {
            val provider: CredentialsProvider = BasicCredentialsProvider()
            provider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(auth.username, auth.password))
            clientBuilder.setDefaultCredentialsProvider(provider)
        }
        val client = clientBuilder.build()

        val targetUrl = urlPrefix + "/" + req.uri.drop("/redirect/".length) + "?" + req.queryParameterString

        log.info { "Redirecting request to targetUrl = $targetUrl" }

        val request = if (req.method == NanoHTTPD.Method.GET) {
            HttpGet(targetUrl)
        } else {
            HttpPost(targetUrl).apply {
                entity = StringEntity(getBody(req))
            }
        }
        val response = client.execute(request)

        val contentType = response.getFirstHeader("Content-Type").value
        val status = convertStatus(response.statusLine)
        val content = inputStreamToString(response.entity.content)

        val headers = response.allHeaders.map { it.name to it.value }.toMap()

        response.close()
        client.close()
        return NanoHTTPD.newFixedLengthResponse(status, contentType, content)//.apply {
//            headers.forEach { (key, value) -> addHeader(key, value)
//            println(key+": "+value)}
//        }
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
    while (inputStream.read(buffer).also { length = it } != -1) {
        result.write(buffer, 0, length)
    }
    return result.toString("UTF-8")
}