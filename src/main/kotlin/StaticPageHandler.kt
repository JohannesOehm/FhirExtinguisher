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
        log.info { "realUri = $realUri, baseUri = $baseUri" }

        if (getPathArray(realUri).contains("..")) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "'..' in URL path is not supported!")
        }

        val resource = javaClass.getResource("static/" + realUri)
        if (resource == null) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 File not found")
        }
        var fileOrDirectory = File(resource.toURI())
        log.info { "file = $fileOrDirectory" }
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory = File(fileOrDirectory, "index.html")
            if (!fileOrDirectory.exists()) {
                fileOrDirectory = File(fileOrDirectory.parentFile, "index.htm")
            }
        }
        return if (!fileOrDirectory.exists() || !fileOrDirectory.isFile) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 File not found")
        } else {
            try {
                newChunkedResponse(
                    Response.Status.OK,
                    getMimeTypeForFile(fileOrDirectory.name),
                    fileToInputStream(fileOrDirectory)
                )
            } catch (ioe: IOException) {
                newFixedLengthResponse(Response.Status.REQUEST_TIMEOUT, "text/plain", null as String?)
            }
        }
    }

    @Throws(IOException::class)
    fun fileToInputStream(fileOrdirectory: File?): BufferedInputStream {
        return BufferedInputStream(FileInputStream(fileOrdirectory))
    }

    private fun getPathArray(uri: String): Array<String> {
        return uri.split("/").filter { it.length > 0 }.toTypedArray()
    }

    fun normalizeUri(value: String?): String? {
        var value = value
        if (value == null) {
            return value
        }
        if (value.startsWith("/")) {
            value = value.substring(1)
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length - 1)
        }
        return value
    }

}