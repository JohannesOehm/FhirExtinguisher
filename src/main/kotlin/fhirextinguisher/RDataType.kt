package fhirextinguisher

import ca.uhn.fhir.model.primitive.BooleanDt
import ca.uhn.fhir.model.primitive.CodeDt
import ca.uhn.fhir.model.primitive.DecimalDt
import ca.uhn.fhir.model.primitive.IntegerDt
import org.hl7.fhir.instance.model.api.IBase

enum class RDataType {
    CHARACTER,
    NUMERIC,
    INTEGER,
    LOGICAL,
    FACTOR,
    COMPLEX;
}

fun parseFhirType(base: IBase) =
    when (base) {
        is BooleanDt -> RDataType.LOGICAL
        is IntegerDt -> RDataType.INTEGER
        is DecimalDt -> RDataType.NUMERIC
        is CodeDt -> RDataType.FACTOR
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