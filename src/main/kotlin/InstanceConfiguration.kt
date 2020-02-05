import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor

data class InstanceConfiguration(
    val fhirServerUrl: String,
    val fhirVersion: FhirContext,
    val fhirInterceptors: List<IClientInterceptor>
)