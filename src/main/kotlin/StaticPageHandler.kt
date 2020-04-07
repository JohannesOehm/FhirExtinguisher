import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.*
import mu.KotlinLogging
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


private val log = KotlinLogging.logger {}

class StaticPageHandler {
    fun serve(session: IHTTPSession): NanoHTTPD.Response {
        val baseUri: String = session.uri
        val realUri: String = normalizeUri(baseUri)!!
        log.info { "Serving '$baseUri', realUri = $realUri" }

        //Protection against path-walking attacks, though it seems like NanoHTTPD is also taking some measures
        if (getPathArray(realUri).contains("..")) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "'..' in URL path is not allowed!")
        }

        val path = "static/$realUri"
        val (resourceStream, isIndexHtml) = if (realUri.isNotEmpty()) {
            javaClass.getResourceAsStream(path) to false
        } else {
            javaClass.getResourceAsStream("static/index.html") to true
        }
        return if (resourceStream != null) {
            try {
                val mimeType = if (isIndexHtml) MIME_HTML else getMimeTypeForFile(realUri)
                newChunkedResponse(Response.Status.OK, mimeType, resourceStream)
            } catch (ioe: IOException) {
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", ioe.message)
            }
        } else {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 File not found")
        }
    }

    @Throws(IOException::class)
    fun fileToInputStream(file: File?): BufferedInputStream {
        return BufferedInputStream(FileInputStream(file))
    }

    private fun getPathArray(uri: String): Array<String> {
        return uri.split("/").filter { it.length > 0 }.toTypedArray()
    }

    /**
     * Remove leading and/or trailing slash
     */
    private fun normalizeUri(value: String?): String? {
        return value?.dropWhile { it == '/' }?.dropLastWhile { it == '/' }
    }

}