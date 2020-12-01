package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource

private val log = KotlinLogging.logger {}

/**
 * There is also the {@link FhirContext#fluentPath}
 */
abstract class FhirPathEngineWrapper(val fhirContext: FhirContext, val fhirClient: IGenericClient) {
    abstract fun parseExpression(expression: String): ExpressionWrapper
    abstract fun evaluateToBase(
        base: IBase,
        expression: ExpressionWrapper,
        variables: Map<String, Any> = emptyMap()
    ): List<IBase>

    abstract fun convertToString(base: IBase): String

    private val cache = mutableMapOf<String, IBaseResource>()

    fun resolve(url: String?): IBaseResource? {
        if (url == null) {
            return null
        }

        return try {
            val reference = if (url.startsWith(fhirClient.serverBase)) {
                url.substring(fhirClient.serverBase.length) //Vonk includes servername in references
            } else {
                url
            }.dropWhile { it == '/' } //remove any leading slashes

            return if (cache.containsKey(reference)) {
                log.info { "Resolving reference '$url' from cache" }
                cache[reference]
            } else {
                log.info { "Resolving reference '$url' from server" }
                val type = reference.split("/", limit = 2)[0] //TODO: There must be a better way to do this
                val requiredClass: Class<IBaseResource> =
                    this.fhirContext.getResourceDefinition(type).implementingClass as Class<IBaseResource>
                val resource = this.fhirClient.fetchResourceFromUrl(requiredClass, url)
                cache[reference] = resource
                resource
            }

        } catch (e: Exception) {
            log.error(e) { "Cannot resolve reference $url!" }
            throw RuntimeException("Failed to resolve reference '$url'!", e)
        }

    }
}



abstract class ExpressionWrapper() {

}


