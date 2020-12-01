package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.dstu3.context.SimpleWorkerContext
import org.hl7.fhir.dstu3.model.*
import org.hl7.fhir.dstu3.model.Enumeration
import org.hl7.fhir.dstu3.utils.FHIRPathEngine
import org.hl7.fhir.instance.model.api.IBase
import java.util.*

class FhirPathEngineWrapperSTU3(fhirContext: FhirContext, fhirClient: IGenericClient) :
    FhirPathEngineWrapper(fhirContext, fhirClient) {
    val engine = FHIRPathEngine(SimpleWorkerContext())

    val variables = mutableMapOf<String, Any>()

    init {
        engine.hostServices = object : org.hl7.fhir.dstu3.utils.FHIRPathEngine.IEvaluationContext {
            override fun resolveConstantType(appContext: Any?, name: String?): org.hl7.fhir.dstu3.model.TypeDetails {
                TODO("not implemented")
            }

            override fun resolveConstant(appContext: Any?, name: String?): Base? {
                val result = variables[name]
                return when (result) {
                    is String -> StringType(result)
                    is Int -> IntegerType(result)
                    else -> null
                }
            }

            override fun resolveFunction(functionName: String?): FHIRPathEngine.IEvaluationContext.FunctionDetails {
                TODO("not implemented")
            }

            override fun checkFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<org.hl7.fhir.dstu3.model.TypeDetails>?
            ): org.hl7.fhir.dstu3.model.TypeDetails {
                TODO("not implemented")
            }

            override fun executeFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<MutableList<Base>>?
            ): MutableList<Base> {
                TODO("not implemented")
            }

            override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
                TODO("not implemented")
            }

            override fun resolveReference(appContext: Any?, url: String?): Base = resolve(url) as Base

        }

    }

    override fun parseExpression(expression: String) =
        ExpressionSTU3(engine.parse(expression))

    override fun convertToString(base: IBase): String = convertToStringInternal(base as Base)


    override fun evaluateToBase(base: IBase, expression: ExpressionWrapper, variables: Map<String, Any>): List<Base> {
        expression as ExpressionSTU3
        base as Base

        this.variables.clear()
        this.variables.putAll(variables)
        return engine.evaluate(base, expression.expression)
    }

    private fun convertToStringInternal(it: Base): String =
        when (it) {
            is Enumeration<*> -> it.value.name
            is Quantity -> ("${it.value} ${it.unit}")
            is DecimalType -> Objects.toString(it.value)
            is CodeableConcept -> if (it.text != null) it.text else it.coding.joinToString { it.code }
            else -> it.toString()
        }
}


class ExpressionSTU3(val expression: ExpressionNode) : ExpressionWrapper() {

}