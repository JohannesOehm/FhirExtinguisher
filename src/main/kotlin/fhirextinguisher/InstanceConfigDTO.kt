package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor
import mu.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}

data class InstanceConfigDTO(
    val fhirServerUrl: String,
    val fhirVersion: String? = null,
    val basicAuth: String? = null,
    val tokenAuth: String? = null,
    val timeoutInMillis: Int = 60_000,
    val blockExternalRequests: Boolean = true,
    val queryStorageFile: String = "savedQueries.csv",
) {
    fun toInstanceConfiguration(): InstanceConfiguration {
        val fhirContext = when (fhirVersion) {
            "stu3", "dstu3" -> FhirContext.forDstu3()
            "r4", null -> FhirContext.forR4()
            else -> throw Exception("Invalid FHIR version string '$fhirVersion'. Valid values are 'stu3', 'dstu3', 'r4'!")
        }

        val auth = if (basicAuth != null) {
            if (!basicAuth.contains(':')) {
                throw Exception("Basic auth credentials must be of format 'username:password'!")
            }
            val (username, passwd) = basicAuth.split(':', limit = 2)
            BasicAuthData(username, passwd)
        } else if (tokenAuth != null) {
            BearerAuthData(tokenAuth)
        } else null

        val interceptors = auth?.let { listOf(it.toInterceptor()) } ?: emptyList()

        fhirContext.restfulClientFactory.connectTimeout = timeoutInMillis
        fhirContext.restfulClientFactory.socketTimeout = timeoutInMillis

        try {
            val client = fhirContext.newRestfulGenericClient(fhirServerUrl)
            interceptors.forEach { client.registerInterceptor(it) }
            client.forceConformanceCheck()
        } catch (e: Exception) {
            log.error { "Error with servers conformance statement: ${e.message}" }
        }

        return InstanceConfiguration(
            fhirServerUrl,
            fhirContext,
            interceptors,
            auth,
            timeoutInMillis,
            blockExternalRequests,
            File(queryStorageFile)
        )
    }
}

sealed class AuthData {
    abstract fun toInterceptor(): IClientInterceptor
}

data class BasicAuthData(
    val username: String,
    val password: String,
) : AuthData() {
    override fun toInterceptor() = BasicAuthInterceptor(username, password)
}

data class BearerAuthData(
    val token: String,
) : AuthData() {
    override fun toInterceptor() = BearerTokenAuthInterceptor(token)
}


data class InstanceConfiguration(
    val fhirServerUrl: String,
    val fhirVersion: FhirContext,
    val interceptors: List<IClientInterceptor>,
    val authData: AuthData?,
    val timeoutInMillis: Int,
    val blockExternalRequests: Boolean,
    val queryStorageFile: File
)

