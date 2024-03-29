package fhirextinguisher

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import mu.KotlinLogging
import org.hl7.fhir.instance.model.api.IBase
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.utils.FHIRPathEngine

/*fun main() {
    val ctx = FhirContext.forR4()
    val simpleWorkerContext = SimpleWorkerContext()
    println(simpleWorkerContext.listStructures())
    val engine = FHIRPathEngine(HapiWorkerContext(ctx, DefaultProfileValidationSupport(ctx)))
    engine.hostServices = object : FHIRPathEngine.IEvaluationContext {
        override fun resolveFunction(functionName: String?): FHIRPathEngine.IEvaluationContext.FunctionDetails? {
            return null
        }

        override fun executeFunction(
            appContext: Any?,
            focus: MutableList<Base>?,
            functionName: String?,
            parameters: MutableList<MutableList<Base>>
        ): MutableList<Base> {
            return mutableListOf()
        }


        override fun checkFunction(
            appContext: Any?,
            functionName: String?,
            parameters: MutableList<TypeDetails>?
        ): TypeDetails =
            TODO("not implemented")

        override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean): Base? {
            println("resolveConstant($appContext, $name, $beforeContext)")
            return null
        }

        override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
            log.info { "argument = $argument, focus = $focus" }
            return true
        }

        override fun resolveReference(appContext: Any?, url: String): Base? {
            println("resolveReference($appContext, $url)")
            return null
        }
        override fun conformsToProfile(appContext: Any?, item: Base?, url: String?): Boolean =
            TODO("not implemented")

        override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails {
            println("resolveConstantType($appContext, $name)")
            val typeDetails = TypeDetails(ExpressionNode.CollectionStatus.SINGLETON, name)
            return typeDetails
            TODO("not implemented")
        }

        override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")


    }

    val fp = engine.parse("subject.resolve()")
    println(fp)
    fp.types
    val result = engine.check(null, "Observation","Observation", fp)
    println(result)
    if(fp.function == ExpressionNode.Function.Resolve) {

    }

} */



class ExpressionR4(val expression: ExpressionNode) : ExpressionWrapper() {
    override fun toString(): String {
        return expression.toString()
    }

}

private val log = KotlinLogging.logger {}

class FhirPathEngineWrapperR4(fhirContext: FhirContext, fhirClient: IGenericClient) :
    FhirPathEngineWrapper(fhirContext, fhirClient) {
    val engine = FHIRPathEngine(SimpleWorkerContext())
    val engine2 = FHIRPathEngine(SimpleWorkerContext())
    val references = mutableListOf<String>()

    val variables = mutableMapOf<String, Any>()

    init {
        engine.hostServices = object : FHIRPathEngine.IEvaluationContext {
            override fun resolveFunction(functionName: String?): FHIRPathEngine.IEvaluationContext.FunctionDetails? {
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
                appContext: Any?,
                focus: MutableList<Base>?,
                functionName: String?,
                parameters: MutableList<MutableList<Base>>
            ): MutableList<Base> {
                return when (functionName) {
                    "getIdPart" -> parameters[0].map { getIdPart(it) }.toMutableList()
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

            override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean): List<Base>? {
                val result = variables[name]
                return when (result) {
                    is String -> listOf(StringType(result))
                    is Int -> listOf(IntegerType(result))
                    else -> null
                }
            }

            override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
                log.info { "argument = $argument, focus = $focus" }
                return true
            }

            override fun resolveReference(appContext: Any?, url: String, refContext: Base?) = resolve(url) as Base

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

        engine2.hostServices = object : FHIRPathEngine.IEvaluationContext {
            override fun resolveFunction(functionName: String?) = null
            override fun executeFunction(
                appContext: Any?,
                focus: MutableList<Base>?,
                functionName: String?,
                parameters: MutableList<MutableList<Base>>
            ) = mutableListOf<Base>()

            override fun checkFunction(
                appContext: Any?,
                functionName: String?,
                parameters: MutableList<TypeDetails>?
            ): TypeDetails? = null

            override fun resolveConstant(appContext: Any?, name: String?, beforeContext: Boolean) = null
            override fun log(argument: String?, focus: MutableList<Base>?): Boolean = true
            override fun conformsToProfile(appContext: Any?, item: Base?, url: String?): Boolean =
                true

            override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails = TODO("not implemented")
            override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")


            override fun resolveReference(appContext: Any?, url: String, refContext: Base?): Base? {
                references.add(url)
                return null
            }

        }

    }

    private fun getIdPart(it: Base): StringType {
        return when (it) {
            is IdType -> StringType(it.idPart)
            is StringType -> StringType(IdType(it.value).idPart)
            is Reference -> StringType(IdType(it.reference).idPart) //TODO: What if identifier is provided and not reference
            is Identifier -> StringType(it.value)
            else -> StringType(it.toString())

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

    override fun evaluateToBase(base: IBase, expression: ExpressionWrapper, variables: Map<String, Any>): List<Base> {
        expression as ExpressionR4
        base as Base
        this.variables.clear()
        this.variables.putAll(variables)
        return engine.evaluate(base, expression.expression)
    }

    override fun convertToString(base: IBase): String {
        base as Base
        return ToStringHelperR4.convertToString(base)
    }

    fun extractReference(resource: Base, expression: String): List<String> {
        engine2.evaluate(resource, expression)
        val result = references.toList()
        references.clear()
        return result
    }


}

