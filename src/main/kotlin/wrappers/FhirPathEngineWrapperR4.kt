package wrappers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IPrimitiveType
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Enumeration
import org.hl7.fhir.r4.utils.FHIRPathEngine
import java.util.*

class ExpressionR4(val expression: ExpressionNode) : ExpressionWrapper() {
    override fun toString(): String {
        return expression.toString()
    }
}

class FhirPathEngineWrapperR4(fhirContext: FhirContext, fhirClient: IGenericClient) :
    FhirPathEngineWrapper(fhirContext, fhirClient) {
    val engine = FHIRPathEngine(SimpleWorkerContext())

    init {
        engine.hostServices = object : org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext {
            override fun resolveFunction(functionName: String?): org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext.FunctionDetails? {
                return when (functionName) {
                    "getIdPart" -> FHIRPathEngine.IEvaluationContext.FunctionDetails(
                        "Tries to string the id to a more human-readable string.",
                        1,
                        1
                    )
                    else -> null
                }

            }

            override fun executeFunction(
                appContext: Any?, functionName: String, parameters: MutableList<MutableList<Base>>
            ): MutableList<Base> {
                return when (functionName) {
                    "getIdPart" -> parameters[0].map { StringType((it as IdType).idPart) }.toMutableList()
                    else -> mutableListOf<Base>()
                }
            }

            override fun checkFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<TypeDetails>?
            ): TypeDetails =
                TODO("not implemented")

            override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean): Base =
                TODO("not implemented")

            override fun log(argument: String?, focus: MutableList<Base>?): Boolean = TODO("not implemented")

            override fun resolveReference(appContext: Any?, url: String): Base? = resolve(url) as Base

            override fun conformsToProfile(appContext: Any?, item: Base?, url: String?): Boolean =
                TODO("not implemented")

            override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails = TODO("not implemented")

            override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")

        }

    }

    override fun parseExpression(expression: String) =
        ExpressionR4(engine.parse(expression))

    override fun evaluateToStringList(base: IBase, expression: ExpressionWrapper): List<String> {
        expression as ExpressionR4
        base as Base

        val evaluation = engine.evaluate(base, expression.expression)
        return evaluation.map { convertToString(it) }
    }

    private fun convertToString(it: Base): String =
        when (it) {
            is Enumeration<*> -> it.code
            is Quantity -> ("${it.value} ${it.unit}")
            is DecimalType -> Objects.toString(it.value)
            is CodeableConcept -> getCodableConceptAsString(it)
            is IPrimitiveType<*> -> it.valueAsString
            is HumanName -> getNameAsString(it)
            is Identifier -> getIdentifierAsString(it)
            is Reference -> getReferenceAsString(it)
            //TODO: Support all of http://hl7.org/fhir/datatypes.html#
            else -> it.toString()
        }

    private fun getCodableConceptAsString(it: CodeableConcept) =
        if (it.text != null) it.text else it.coding.joinToString { it.code }

    private fun getNameAsString(name: HumanName): String {
        if (name.hasText()) {
            return name.text
        } else {
            return listOf(
                name.prefix.joinToString(" "),
                name.given.joinToString(" "),
                if (name.hasFamily()) name.family else "",
                name.suffix.joinToString(" ")
            ).joinToString(" ") + (if (name.hasUse()) " (${name.use.toCode()})" else "")
        }
    }

    private fun getReferenceAsString(reference: Reference): String {
        if (reference.hasReference()) {
            return reference.reference
        } else if (reference.hasIdentifier()) {
            return getIdentifierAsString(reference.identifier)
        } else {
            return reference.display
        }
    }

    private fun getIdentifierAsString(identifier: Identifier): String {
        val idPart = (if (identifier.hasSystem()) identifier.system else "") + "|" + identifier.value

        val bracketPart = if (identifier.hasType() && identifier.hasUse()) {
            " (${getCodableConceptAsString(identifier.type)}, ${identifier.use.toCode()})"
        } else if (identifier.hasUse()) {
            " (${identifier.use.toCode()})"
        } else if (identifier.hasType()) {
            " (${getCodableConceptAsString(identifier.type)})"
        } else {
            ""
        }
        val period = if (identifier.hasPeriod()) " [" + identifier.period + "]" else "" //TODO: period.toString() ?
        val assigner =
            if (identifier.hasAssigner()) " {" + identifier.assigner + "}" else "" // TODO: identifier.assginer.toString()
        return idPart + bracketPart + period + assigner
    }
}
