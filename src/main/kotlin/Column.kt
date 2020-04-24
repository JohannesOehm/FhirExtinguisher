import wrappers.ExpressionWrapper

data class Column(
    val name: String,
    val expression: ExpressionWrapper,
    val listProcessingMode: String
)