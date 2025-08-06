package de.unimuenster.imi.fhir.transform

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import mu.KotlinLogging
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.instance.model.api.IPrimitiveType
import kotlin.Int

class BundleTransformer(private val fhirContext: FhirContext) {
    private val log = KotlinLogging.logger("de.unimuenster.imi.fhir.transform.BundleTransformer")
    private val fhirClient = fhirContext.newRestfulGenericClient("http://local/fhir")
    private val jsonParser = fhirContext.newJsonParser()
    private val fhirPathEngine = if (fhirContext.version.version == FhirVersionEnum.DSTU3) {
        FhirPathEngineWrapperSTU3(fhirContext, fhirClient)
    } else {
        FhirPathEngineWrapperR4(fhirContext, fhirClient)
    }

    fun getResourceTypesInBundle(resourceString: String): Set<String> {
        val resource = readInResource(resourceString)
        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)

        return bundleWrapper.entry
            .mapNotNull { it.resource?.fhirType() }
            .toSet()
    }

    fun processBundle(
        resourceString: String, transformationParameters: TransformationParameters
    ): ResultTable {
        val resource = readInResource(resourceString)
        val bundleDefinition = fhirContext.getResourceDefinition("Bundle")
        val bundleWrapper = BundleWrapper(bundleDefinition, resource)

        val resultTables = mutableListOf<SubTable>()
        val filteredEntries = bundleWrapper.getFilteredEntries(transformationParameters.resourcesToKeepInTable)
        for (bundleEntry in filteredEntries) {

            val flattenedEntry = processBundleEntry(
                transformationParameters,
                bundleEntry,
                if (transformationParameters.addRaw) jsonParser.encodeResourceToString(bundleEntry.resource as IBaseResource) else null
            )

            resultTables += if (transformationParameters.useExtendedWideFormatColumnHeaders) {
                addIndicesToWideColumnNamesWithFhirPath(bundleEntry, flattenedEntry)
            } else {
                flattenedEntry
            }
        }

        return ResultTable(resultTables)
    }

    private fun BundleWrapper.getFilteredEntries(typesToKeep: List<String>): List<BundleEntryComponentWrapper> {
        return this.entry.filter { it.resource?.fhirType() in typesToKeep }
    }


    private fun readInResource(resourceString: String): IBaseResource {
        val resource: IBaseResource
        try {
            resource = jsonParser.parseResource(resourceString)
        } catch (e: Exception) {
            log.error("Could not convert resource to Bundle instance: ", e)
            throw Exception("ConversionError")
        }
        return resource
    }

    private fun processBundleEntry(
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

    private fun String.toFhirExpression() = fhirPathEngine.parseExpression(this)

    private fun addIndicesToWideColumnNamesWithFhirPath(bundleEntry: BundleEntryComponentWrapper, table: SubTable): SubTable {
        return SubTable().apply {
            table.data.forEach { (header, entry) ->
                val columnName = header.second
                val newColumnName = buildColumnName(columnName, bundleEntry, entry)
                this.addColumn(newColumnName, entry[0]!!)
            }
        }
    }

    private fun buildColumnName(
        columnName: String,
        bundleEntry: BundleEntryComponentWrapper,
        entry: List<String?>
    ): String {
        val columnNameWithoutTrailingIndex = columnName.substringBeforeLast(".")
        val splittedPath = columnNameWithoutTrailingIndex.split(".")
        val rootNode = splittedPath[0]
        val remainingPath = splittedPath.drop(1)

        val result = StringBuilder(rootNode)
        var currentEntries = listOf(bundleEntry.resource!!)

        remainingPath.forEach { currentPart ->
            val evaluatedEntries = currentEntries.flatMap {
                fhirPathEngine.evaluateToBase(it, currentPart.toFhirExpression())
            }

            val entryIndex = evaluatedEntries.findIndexInHierarchy(remainingPath.drop(1), entry.firstOrNull())

            result.append(".$currentPart").append("[${entryIndex ?: 0}]")
            currentEntries = if (evaluatedEntries.isNotEmpty()) listOf(evaluatedEntries[entryIndex ?: 0])
                                else emptyList()
        }

        return result.toString()
    }

    private fun List<IBase>.findIndexInHierarchy(
        remainingPath: List<String>,
        targetValue: String?
    ): Int? {
        return this.mapIndexedNotNull { index, foundEntry ->
            val subPath = remainingPath.joinToString(".")
            when {
                remainingPath.isEmpty() && isPrimitiveMatch(foundEntry, targetValue) -> index
                remainingPath.isNotEmpty() && isPrimitiveMatch(foundEntry, targetValue) -> index
                remainingPath.isNotEmpty() && isNestedMatch(foundEntry, subPath, targetValue) -> index
                else -> null
            }


        }.firstOrNull()
    }

    private fun isPrimitiveMatch(entry: Any, targetValue: String?): Boolean {
        return entry is IPrimitiveType<*> && entry.toString() == targetValue
    }

    private fun isNestedMatch(entry: IBase, subPath: String, targetValue: String?): Boolean {
        val nestedEval = fhirPathEngine.evaluateToBase(entry, subPath.toFhirExpression())
        return nestedEval.any { it.toString() == targetValue }
    }


}