package wrappers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.utils.FHIRPathEngine

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
                    "getChildFields" -> FHIRPathEngine.IEvaluationContext.FunctionDetails(
                        "Returns the possible children names of a type",
                        1,
                        1
                    )
                    "stringify" -> FHIRPathEngine.IEvaluationContext.FunctionDetails(
                        "Writes the result as serialized JSON object",
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
                    "getChildFields" -> parameters[0][0].children().map { StringType(it.name) }.toMutableList()
                    "stringify" -> parameters[0].map { StringType(stringifyElement(it)) }.toMutableList()
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

            override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails {
                println("resolveConstantType(appContext=$appContext, name=$name)")
                val typeDetails = TypeDetails(ExpressionNode.CollectionStatus.SINGLETON, "name")
                return typeDetails
                TODO("not implemented")
            }

            override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")


        }

    }

    private fun stringifyElement(it: Base): String? {
        return stringifyElement(it, "", false)
    }

    private fun stringifyElement(it: Base, prefix: String, isList: Boolean): String? {
        if (it.isPrimitive) {
            return it.toString()
        }

        val sb = StringBuilder()
        for ((index, child) in it.children().filter { it.hasValues() }.withIndex()) {
            val prefix1 = if (isList && index == 0) prefix.substring(2) + "- " else prefix
            sb.append(prefix1).append(child.name).append(": ")
            if (child.values.all { it.isPrimitive }) {
                if (child.isList) {
                    sb.append("[")
                }
                sb.append(child.values.joinToString())
                if (child.isList) {
                    sb.append("]")
                }
            } else {
                for (value in child.values) {
                    sb.append("\n").append(stringifyElement(value, "$prefix    ", child.isList))
                }
            }
            sb.append("\n")
        }
        return sb.toString().dropLast(1) //drop last linebreak

    }

    override fun parseExpression(expression: String) =
        ExpressionR4(engine.parse(expression))

    fun getTypeDetails(expression: String) {
        engine.check(null, "Patient", null, "Patient")
    }

    override fun evaluateToBase(base: IBase, expression: ExpressionWrapper): List<Base> {
        expression as ExpressionR4
        base as Base
        return engine.evaluate(base, expression.expression)
    }

    override fun evaluateToStringList(base: IBase, expression: ExpressionWrapper): List<String> {
        expression as ExpressionR4
        base as Base

        return evaluateToBase(base, expression).map { ToStringHelperR4.convertToString(it) }
    }


}

