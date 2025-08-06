package de.unimuenster.imi.fhir.transform

import ca.uhn.fhir.context.FhirContext
import de.unimuenster.imi.fhir.columns_parser.Column
import org.apache.commons.csv.CSVFormat

data class TransformationParameters(
    val csvFormat: CSVFormat,
    val limit: Int?,
    val columns: List<Column>?,
    val addRaw: Boolean = false,
    val addResourceNameToColumn: Boolean = false,
    val useExtendedWideFormatColumnHeaders: Boolean = false,
    val resourcesToKeepInTable: List<String> = FhirContext.forR4().resourceTypes.toList()
)