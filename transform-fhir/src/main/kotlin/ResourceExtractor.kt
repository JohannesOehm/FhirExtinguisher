import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.dstu3.hapi.ctx.FhirDstu3
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.hapi.ctx.FhirR4
import java.io.InputStream

abstract class ResourceExtractor {
    companion object {
        fun forR4(): ResourceExtractor {
            return ResourceExtractorR4()
        }

        fun forSTU3(): ResourceExtractor {
            return ResourceExtractorSTU3()
        }

        fun forFhirContext(context: FhirContext): ResourceExtractor {
            return when (context.version) {
                FhirR4::class -> forR4()
                FhirDstu3::class -> forSTU3()
                else -> forR4()
            }
        }
    }

    lateinit var processingMode: ListProcessingMode

    fun setJoin(delimiter: String = " ") {
        this.processingMode = Join(delimiter)
    }

    fun setExplodeWide(discriminator: String = "%index") {
        this.processingMode = ExplodeWide(discriminator, arrayOf())
    }

    fun setExplodeLong() {
        this.processingMode = ExplodeLong(arrayOf())
    }

    fun removeResourceName(expression: String): String {
        val index = expression.indexOf(".")
        return if (index != -1) expression.substring(index + 1) else expression
    }

    abstract fun getResourceNames(): List<String>

    abstract fun getResourceFields(resourceName: String): List<Column>

    abstract fun getResourceFieldsForEntriesInBundle(rawBundle: String): List<Column>

    abstract fun loadBundleFromFile(resourceStream: InputStream): IBase

    abstract fun loadStructureDefinitionsResource(): IBase

    abstract fun loadStructureDefinitionsTypes(): IBase
}