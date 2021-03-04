package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource
import java.util.*


private val log = KotlinLogging.logger {}

class MaxSizeHashMap<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
        return size > maxSize
    }
}

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

    private val cache = MaxSizeHashMap<String, IBaseResource>(1000)

    fun clearCache() {
        cache.clear()
    }

    fun addCacheEntry(id: String, base: IBaseResource) {
        log.debug { "addCacheEntry('$id', $base). cache.size = ${cache.size}" }
        cache[id] = base
    }


    fun resolve(url: String?): IBaseResource? {
        if (url == null) {
            return null
        }

        try {
            val reference = if (url.startsWith(fhirClient.serverBase)) {
                url.substring(fhirClient.serverBase.length) //Vonk includes servername in references
            } else {
                url
            }.dropWhile { it == '/' } //remove any leading slashes

            return if (cache.containsKey(reference)) {
                log.debug { "Resolving reference '$url' from cache" }
                cache[reference]
            } else {
                log.info { "Resolving reference '$url' from server" }
                val type = reference.split("/", limit = 2)[0] //TODO: There must be a better way to do this
                val requiredClass: Class<IBaseResource> =
                    this.fhirContext.getResourceDefinition(type).implementingClass as Class<IBaseResource>
                val resource = this.fhirClient.fetchResourceFromUrl(requiredClass, url)
                addCacheEntry(reference, resource)
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


