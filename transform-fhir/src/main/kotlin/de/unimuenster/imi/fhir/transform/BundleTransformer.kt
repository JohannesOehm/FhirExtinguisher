package de.unimuenster.imi.fhir.transform

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import mu.KotlinLogging
import org.apache.commons.csv.CSVPrinter
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource

class BundleTransformer(private val fhirContext: FhirContext) {
    private val log = KotlinLogging.logger("de.unimuenster.imi.fhir.transform.BundleTransformer")
    private val fhirClient = fhirContext.newRestfulGenericClient("http://local/fhir")
    private val fhirPathEngine = if (fhirContext.version.version == FhirVersionEnum.DSTU3) {
        FhirPathEngineWrapperSTU3(fhirContext, fhirClient)
    } else {
        FhirPathEngineWrapperR4(fhirContext, fhirClient)
    }

    fun processBundle(
        resourceString: String, transformationParameters: TransformationParameters
    ): String {
        val jsonParser = fhirContext.newJsonParser()

        val resource: IBase
        try {
            resource = jsonParser.parseResource(resourceString)
        } catch (e: Exception) {
            log.error("Could not convert resource to Bundle instance: ", e)
            throw Exception("ConversionError")
        }

        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)

        val resultTables = mutableListOf<SubTable>()
        for (bundleEntry in bundleWrapper.entry) {
            resultTables += processBundleEntry(
                transformationParameters,
                bundleEntry,
                if (transformationParameters.addRaw) jsonParser.encodeResourceToString(bundleEntry.resource as IBaseResource) else null
            )
        }

        val resultTable = ResultTable(resultTables)
        val sb = StringBuilder()
        val printer = CSVPrinter(sb, transformationParameters.csvFormat)

        resultTable.print(printer)
        return sb.toString()
    }

    fun processBundleEntry(
        transformationParameters: TransformationParameters,
        bundleEntry: BundleEntryComponentWrapper,
        addRaw: String? = null
    ): SubTable {
        val table = SubTable()
        if (addRaw != null) {
            if (transformationParameters.addResourceNameToColumn) {
                table.addColumn("${bundleEntry.resource?.fhirType()}.\$raw", addRaw)
            } else table.addColumn("\$raw", addRaw)
        }

        for (column in transformationParameters.columns!!) {
            try {
                table.addColumn(
                    column,
                    bundleEntry.resource!!,
                    fhirPathEngine,
                    transformationParameters.addResourceNameToColumn
                )
            } catch (e: Exception) {
                if (transformationParameters.addResourceNameToColumn) {
                    table.addColumn(
                        "${bundleEntry.resource?.fhirType()}.${column.name}",
                        e.message ?: "ERROR"
                    )
                } else {
                    table.addColumn(column.name, e.message ?: "ERROR")
                }
            }
        }
        return table
    }

}