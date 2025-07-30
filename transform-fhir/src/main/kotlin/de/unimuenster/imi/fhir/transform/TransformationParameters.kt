package de.unimuenster.imi.fhir.transform

import de.unimuenster.imi.fhir.columns_parser.Column
import org.apache.commons.csv.CSVFormat

data class TransformationParameters(
    val csvFormat: CSVFormat,
    val limit: Int?,
    val columns: List<Column>?,
    val addRaw: Boolean = false,
    val addResourceNameToColumn: Boolean = false
)