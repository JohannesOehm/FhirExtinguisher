package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import java.io.File

data class InstanceConfigDTO(
    val fhirServerUrl: String,
    val fhirVersion: String? = null,
    val basicAuth: String?,
    val blockExternalRequests: Boolean,
    val queryStorageFile: String,
) {
    fun toInstanceConfiguration(): InstanceConfiguration {
        val fhirContext = when (fhirVersion) {
            "stu3", "dstu3" -> FhirContext.forDstu3()
            "r4", null -> FhirContext.forR4()
            else -> throw Exception("Invalid FHIR version string '$fhirVersion'. Valid values are 'stu3', 'dstu3', 'r4'!")
        }

        val (interceptors, basicAuth) = if (basicAuth != null) {
            if (!basicAuth.contains(':')) {
                throw Exception("Basic auth credentials must be of format 'username:password'!")
            }
            val (username, passwd) = basicAuth.split(':', limit = 2)
            listOf(BasicAuthInterceptor(username, passwd)) to BasicAuthData(username, passwd)
        } else emptyList<IClientInterceptor>() to null

        fhirContext.newRestfulGenericClient(fhirServerUrl).forceConformanceCheck()

        return InstanceConfiguration(
            fhirServerUrl,
            fhirContext,
            interceptors,
            blockExternalRequests,
            File(queryStorageFile)
        )
    }
}

data class BasicAuthData(
    val username: String,
    val password: String
)

data class InstanceConfiguration(
    val fhirServerUrl: String,
    val fhirVersion: FhirContext,
    val interceptors: List<IClientInterceptor>,
    val blockExternalRequests: Boolean,
    val queryStorageFile: File
)

