package wrappers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
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

private val log = KotlinLogging.logger {}

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

            override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
                log.info { "argument = $argument, focus = $focus" }
                return true
            }

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
        return evaluation.map { ToStringHelperR4.convertToString(it) }
    }


}

