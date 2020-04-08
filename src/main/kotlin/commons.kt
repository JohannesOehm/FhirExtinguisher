import fi.iki.elonen.NanoHTTPD

fun getBody(session: NanoHTTPD.IHTTPSession): String? {
    val foo = HashMap<String, String>()
    session.parseBody(foo)
    return foo["postData"]
}