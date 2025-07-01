import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import mu.KotlinLogging
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource

class BundleTransformer(private val fhirContext: FhirContext) {
    private val log = KotlinLogging.logger("BundleTransformer")
    private val fhirClient = fhirContext.newRestfulGenericClient("http://local/fhir")
    private val fhirPathEngine = if (fhirContext.version.version == FhirVersionEnum.DSTU3) {
        FhirPathEngineWrapperSTU3(fhirContext, fhirClient)
    } else {
        FhirPathEngineWrapperR4(fhirContext, fhirClient)
    }

    fun processBundle(
        resourceString: String,
        transformationParameters: TransformationParameters
    ): String {
        val jsonParser = fhirContext.newJsonParser()

        val resource: IBase
        try {
             resource = jsonParser.parseResource(resourceString)
        } catch(e: Exception) {
            log.error("Could not convert resource to Bundle instance: ", e)
            throw Exception("ConversionError")
        }

        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)

        val resultTables = mutableListOf<SubTable>()
        for (bundleEntry in bundleWrapper.entry) {
            resultTables += processBundleEntry(
                transformationParameters.columns!!,
                bundleEntry,
                jsonParser.encodeResourceToString(bundleEntry.resource as IBaseResource)
            )
        }

        val resultTable = ResultTable(resultTables)
        val sb = StringBuilder()
        val printer = CSVPrinter(sb, transformationParameters.csvFormat)

        resultTable.print(printer)
        return sb.toString()
    }

    fun processBundleEntry(
        columns: List<Column>,
        bundleEntry: BundleEntryComponentWrapper,
        addRaw: String? = null
    ): SubTable {
        val table = SubTable()
        if (addRaw != null) {
            table.addColumn("\$raw", addRaw)
        }

        for (column in columns) {
            try {
                table.addColumn(column, bundleEntry.resource!!, fhirPathEngine)
            } catch (e: Exception) {
                table.addColumn(column.name, e.message ?: "ERROR")
            }
        }
        return table
    }

}