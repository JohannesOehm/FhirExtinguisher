import fi.iki.elonen.NanoHTTPD

class MyRouters(portnumber: Int, private val instanceConfiguration: InstanceConfiguration) : NanoHTTPD(portnumber) {

    var fhirExtinguisher = FhirExtinguisher(
        instanceConfiguration.fhirServerUrl,
        instanceConfiguration.fhirVersion,
        instanceConfiguration.fhirInterceptors
    )
    var staticPageHandler = StaticPageHandler()

    var redirectService = RedirectService(instanceConfiguration.fhirServerUrl)

    override fun serve(session: IHTTPSession): Response {
        if (session.uri.startsWith("/fhir/")) {
            return fhirExtinguisher.serve(session)
        } else if (session.uri.startsWith("/redirect/")) {
            return redirectService.serve(session)
        } else {
            return staticPageHandler.serve(session)
        }
    }
}