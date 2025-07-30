package de.unimuenster.imi.fhir.transform

import de.unimuenster.imi.fhir.columns_parser.Column
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import mu.KotlinLogging
import org.hl7.fhir.dstu3.model.*
import java.io.InputStream


class ResourceExtractorSTU3: ResourceExtractor() {
    private val fhirContext = FhirContext.forDstu3()
    private val jsonParser = fhirContext.newJsonParser()
    private val structureDefinitionsResource: Bundle
    private val structureDefinitionsTypes: Bundle

    init {
        // Replace these with the appropriate loading mechanisms for your HAPI FHIR resources
        structureDefinitionsResource = loadStructureDefinitionsResource()
        structureDefinitionsTypes = loadStructureDefinitionsTypes()
        this.setExplodeWide()
        this.log = KotlinLogging.logger("de.unimuenster.imi.fhir.transform.ResourceExtractorSTU3")
        this.fpe = FhirPathEngineWrapperSTU3(this.fhirContext,
            this.fhirContext.newRestfulGenericClient("http://example.com"))
    }


    override fun getResourceNames(): List<String> {
        return structureDefinitionsResource.entry
            .mapNotNull { it.resource as? StructureDefinition }
            .filter { it.kind == StructureDefinition.StructureDefinitionKind.RESOURCE }
            .map { it.name }
    }

    override fun getResourceFields(resourceName: String): List<Column> {
        val resources = structureDefinitionsResource.entry
            .mapNotNull { it.resource as? StructureDefinition }
            .filter { it.name == resourceName }

        if (resources.isEmpty()) {
            println("Invalid resource name: $resourceName")
            return emptyList()
        }

        val result = mutableListOf<Column>()
        val structureDefinition = resources.first()

        structureDefinition.snapshot?.element
            ?.filterNot { it.path == resourceName || it.base?.path == "Element.id" }
            ?.flatMap { elementDefinition ->
                val expression = elementDefinition.path
                val name = removeResourceName(expression)
                val types = elementDefinition.type

                when {
                    types.size == 1 -> processSingleType(elementDefinition, name, expression)
                    types.size > 1 -> processMultipleTypes(types, expression, name)
                    else -> emptyList()
                }
            }
            ?.filterNot { it.expression.contains("extension") || it.expression.contains("modifierExtension") }
            ?.let { result.addAll(it) }

        return result
    }

    override fun getResourceFieldsForEntriesInBundle(rawBundle: String): List<Column> {
        val bundle = this.jsonParser.parseResource(rawBundle) as org.hl7.fhir.r4.model.Bundle

        return bundle.entry
            .mapNotNull { it.resource } // Get all non-null resources in the bundle
            .flatMap { resource ->
                val resourceType = resource.resourceType.toString()

                // Filter fields based on their presence in the resource
                getResourceFields(resourceType).filter { column ->
                    isFieldPresentInResource(resource, column.expression)
                }
            }
            .distinct() // Ensure only unique fields are collected
            .toMutableList()
    }

    private fun processSingleType(
        elementDefinition: ElementDefinition,
        name: String,
        expression: String
    ): List<Column> {
        val dataType = elementDefinition.type.firstOrNull()?.code?.let { getDataType(it) }

        return when {
            elementDefinition.base?.path == "Resource.id" -> listOf(Column(name, "getIdPart($expression)", this.processingMode))
            dataType != null -> processDataType(dataType, name, expression)
            else -> listOf(Column(name, expression, this.processingMode))
        }
    }

    private fun processDataType(
        dataType: StructureDefinition,
        name: String,
        expression: String
    ): List<Column> {
        return when {
            dataType.kind == StructureDefinition.StructureDefinitionKind.PRIMITIVETYPE -> listOf(Column(name, expression, this.processingMode))
            dataType.snapshot != null -> {
                val result = mutableListOf<Column>()
                addTypeElements(dataType, expression, expression, result)
                result
            }
            else -> listOf(Column(name, expression, this.processingMode))
        }
    }

    private fun processMultipleTypes(
        types: List<ElementDefinition.TypeRefComponent>,
        expression: String,
        name: String
    ): List<Column> {
        val startExpression = expression.substringBefore("[x]")

        return types.flatMap { type ->
            val dataType = getDataType(type.code)
            val expressionWithCasting = "$startExpression.ofType(${dataType?.name})"
            val newName = "$startExpression${dataType?.name?.replaceFirstChar { it.uppercaseChar() }}"

            if (dataType?.kind == StructureDefinition.StructureDefinitionKind.PRIMITIVETYPE) {
                listOf(Column(removeResourceName(newName), expressionWithCasting, this.processingMode))
            } else {
                val result = mutableListOf<Column>()
                addTypeElements(dataType, expressionWithCasting, newName, result)
                result
            }
        }
    }

    private fun addTypeElements(
        dataType: StructureDefinition?,
        expression: String,
        name: String,
        result: MutableList<Column>
    ) {
        dataType?.snapshot?.element?.forEach { elementDefinition ->
            val subPath = removeResourceName(elementDefinition.path)
            if (elementDefinition.base?.path != "Element.id" && elementDefinition.path != dataType.id) {
                val expression = "$expression.$subPath"
                val newName = "$name.$subPath"
                result.add(Column(removeResourceName(newName), expression, this.processingMode))
            }
        }
    }

    private fun getDataType(dataTypeName: String): StructureDefinition? {
        return structureDefinitionsTypes.entry
            .mapNotNull { it.resource as? StructureDefinition }
            .firstOrNull { it.name == dataTypeName }
    }

    override fun loadBundleFromFile(resourceStream: InputStream): Bundle {
        val parser: IParser = fhirContext.newJsonParser()
        val fileContent = resourceStream.bufferedReader().use { it.readText() }
        return parser.parseResource(Bundle::class.java, fileContent)
    }

    override fun loadStructureDefinitionsResource(): Bundle {
        val resourcePath = "fhir/stu3/profiles-resources.json"

        // Get the resource as a stream
        val resourceStream = this::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        // Load the bundle from the resource stream
        return loadBundleFromFile(resourceStream)
    }


    override fun loadStructureDefinitionsTypes(): Bundle {
        val resourcePath = "fhir/stu3/profiles-types.json"

        // Get the resource as a stream
        val resourceStream = this::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        // Load the bundle from the resource stream
        return loadBundleFromFile(resourceStream)
    }
}
