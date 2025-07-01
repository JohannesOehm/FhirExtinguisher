import org.apache.commons.csv.CSVFormat

data class TransformationParameters(
    val csvFormat: CSVFormat,
    val limit: Int?,
    val columns: List<Column>?
)