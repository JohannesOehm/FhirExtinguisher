package fhirextinguisher

import kotlinx.serialization.Serializable
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.model.*

@Serializable
enum class RDataType {
    CHARACTER,
    NUMERIC,
    INTEGER,
    LOGICAL,
    FACTOR,
    DATE,
    TIME,
    DATETIME,
    COMPLEX;
}

fun parseFhirType(base: IBase) =
    when (base) {
        is BooleanType -> RDataType.LOGICAL
        is IntegerType -> RDataType.INTEGER
        is DecimalType -> RDataType.NUMERIC
        is CodeType, is Enumeration<*> -> RDataType.FACTOR
        is DateType -> RDataType.DATE
        is TimeType -> RDataType.TIME
        is DateTimeType -> RDataType.DATETIME
        else -> RDataType.CHARACTER
    }

fun parseFhirTypes(bases: List<IBase>): RDataType {
    val datatypes = bases.map(::parseFhirType).distinct().toSet()
    if (datatypes.size == 1) {
        return datatypes.single()
    } else if (datatypes == setOf(RDataType.NUMERIC, RDataType.INTEGER)) {
        return RDataType.NUMERIC
    } else {
        return RDataType.CHARACTER
    }
}