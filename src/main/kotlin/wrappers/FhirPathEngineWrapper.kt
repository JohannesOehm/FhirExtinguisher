package wrappers

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
import org.hl7.fhir.dstu3.model.Base
import org.hl7.fhir.dstu3.utils.FHIRPathEngine
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.TypeDetails
import org.hl7.fhir.r4.model.ValueSet
import java.util.*
import org.hl7.fhir.dstu3.context.SimpleWorkerContext as SimpleWorkerContextSTU3
import org.hl7.fhir.dstu3.model.Base as BaseR3
import org.hl7.fhir.dstu3.model.CodeableConcept as CodeableConceptSTU3
import org.hl7.fhir.dstu3.model.DecimalType as DecimalTypeSTU3
import org.hl7.fhir.dstu3.model.Enumeration as EnumerationSTU3
import org.hl7.fhir.dstu3.model.ExpressionNode as ExpressionNodeSTU3
import org.hl7.fhir.dstu3.model.Quantity as QuantitySTU3
import org.hl7.fhir.dstu3.utils.FHIRPathEngine as FHIRPathEngineSTU3
import org.hl7.fhir.r4.context.SimpleWorkerContext as SimpleWorkerContextR4
import org.hl7.fhir.r4.model.Base as BaseR4
import org.hl7.fhir.r4.model.CodeableConcept as CodeableConceptR4
import org.hl7.fhir.r4.model.DecimalType as DecimalTypeR4
import org.hl7.fhir.r4.model.Enumeration as EnumerationR4
import org.hl7.fhir.r4.model.ExpressionNode as ExpressionNodeR4
import org.hl7.fhir.r4.model.Quantity as QuantityR4
import org.hl7.fhir.r4.utils.FHIRPathEngine as FHIRPathEngineR4

private val log = KotlinLogging.logger {}

/**
 * There is also the {@link FhirContext#fluentPath}
 */
abstract class FhirPathEngineWrapper(val fhirContext: FhirContext, val fhirClient: IGenericClient) {
    abstract fun parseExpression(expression: String): ExpressionWrapper
    abstract fun evaluateToStringList(base: IBase, expression: ExpressionWrapper): List<String>

    private val cache = mutableMapOf<String, IBaseResource>()

    fun resolve(url: String?): IBaseResource? {
        if (url == null) {
            return null
        }

        return try {
            val reference = if (url.startsWith(fhirClient.serverBase)) {
                url.substring(fhirClient.serverBase.length) //Vonk includes servername in references
            } else {
                url
            }.dropWhile { it == '/' } //remove any leading slashes

            return if (cache.containsKey(reference)) {
                log.info { "Resolving reference $url from cache" }
                cache[reference]
            } else {
                log.info { "Resolving reference $url from server" }
                val type = reference.split("/", limit = 2)[0] //TODO: There must be a better way to do this
                val requiredClass: Class<IBaseResource> =
                    this.fhirContext.getResourceDefinition(type).implementingClass as Class<IBaseResource>
                val resource = this.fhirClient.fetchResourceFromUrl(requiredClass, url)
                cache[url] = resource
                resource
            }

        } catch (e: Exception) {
            log.error(e) { "Cannot resolve reference $url!" }
            null
        }

    }
}

class FhirPathEngineWrapperR4(fhirContext: FhirContext, fhirClient: IGenericClient) :
    FhirPathEngineWrapper(fhirContext, fhirClient) {
    val engine = FHIRPathEngineR4(SimpleWorkerContextR4())

    init {
        engine.hostServices = object : org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext {
            override fun resolveFunction(functionName: String?): org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext.FunctionDetails {
                TODO("not implemented")
            }

            override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean): BaseR4 =
                TODO("not implemented")

            override fun checkFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<TypeDetails>?
            ): TypeDetails =
                TODO("not implemented")

            override fun log(argument: String?, focus: MutableList<BaseR4>?): Boolean = TODO("not implemented")

            override fun resolveReference(appContext: Any?, url: String): BaseR4? = resolve(url) as BaseR4

            override fun conformsToProfile(appContext: Any?, item: BaseR4?, url: String?): Boolean =
                TODO("not implemented")

            override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails = TODO("not implemented")

            override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")

            override fun executeFunction(
                appContext: Any?, functionName: String?, parameters: MutableList<MutableList<BaseR4>>?
            ): MutableList<org.hl7.fhir.r4.model.Base> =
                TODO("not implemented")
        }

    }

    override fun parseExpression(expression: String) =
        ExpressionR4(engine.parse(expression))

    override fun evaluateToStringList(base: IBase, expression: ExpressionWrapper): List<String> {
        expression as ExpressionR4
        base as BaseR4

        val evaluation = engine.evaluate(base, expression.expression)
        return evaluation.map { convertToString(it) }
    }

    private fun convertToString(it: BaseR4): String =
        when (it) {
            is EnumerationR4<*> -> it.code
            is QuantityR4 -> ("${it.value} ${it.unit}")
            is DecimalTypeR4 -> Objects.toString(it.value)
            is CodeableConceptR4 -> if (it.text != null) it.text else it.coding.joinToString { it.code }
            else -> it.toString()
        }
}

class FhirPathEngineWrapperSTU3(fhirContext: FhirContext, fhirClient: IGenericClient) :
    FhirPathEngineWrapper(fhirContext, fhirClient) {
    val engine = FHIRPathEngineSTU3(SimpleWorkerContextSTU3())

    init {
        engine.hostServices = object : org.hl7.fhir.dstu3.utils.FHIRPathEngine.IEvaluationContext {
            override fun resolveConstantType(appContext: Any?, name: String?): org.hl7.fhir.dstu3.model.TypeDetails {
                TODO("not implemented")
            }

            override fun resolveFunction(functionName: String?): FHIRPathEngine.IEvaluationContext.FunctionDetails {
                TODO("not implemented")
            }

            override fun resolveConstant(appContext: Any?, name: String?): Base {
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

            override fun resolveReference(appContext: Any?, url: String?): Base = resolve(url) as BaseR3

        }

    }

    override fun parseExpression(expression: String) =
        ExpressionSTU3(engine.parse(expression))

    override fun evaluateToStringList(base: IBase, expression: ExpressionWrapper): List<String> {
        expression as ExpressionSTU3
        base as BaseR3
        val expression1 = expression.expression
        val evaluation = engine.evaluate(base, expression1)
        return evaluation.map { convertToString(it) }
    }

    private fun convertToString(it: BaseR3): String =
        when (it) {
            is EnumerationSTU3<*> -> it.value.name
            is QuantitySTU3 -> ("${it.value} ${it.unit}")
            is DecimalTypeSTU3 -> Objects.toString(it.value)
            is CodeableConceptSTU3 -> if (it.text != null) it.text else it.coding.joinToString { it.code }
            else -> it.toString()
        }
}

abstract class ExpressionWrapper() {

}

class ExpressionR4(val expression: ExpressionNodeR4) : ExpressionWrapper() {

}

class ExpressionSTU3(val expression: ExpressionNodeSTU3) : ExpressionWrapper() {

}