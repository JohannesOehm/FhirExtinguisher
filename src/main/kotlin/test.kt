import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.utils.FHIRPathEngine

fun main() {

    val engine = FHIRPathEngine(SimpleWorkerContext())
    engine.hostServices = object : FHIRPathEngine.IEvaluationContext {
        override fun resolveFunction(functionName: String?): org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext.FunctionDetails? {
            return null
        }

        override fun executeFunction(
            appContext: Any?, functionName: String, parameters: MutableList<MutableList<Base>>
        ): MutableList<Base> {
            return mutableListOf<Base>()
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
            return true
        }

        override fun resolveReference(appContext: Any?, url: String): Base? = TODO()

        override fun conformsToProfile(appContext: Any?, item: Base?, url: String?): Boolean =
            TODO("not implemented")

        override fun resolveConstantType(appContext: Any?, name: String?): TypeDetails {
            println("resolveConstantType(appContext=$appContext, name=$name)")
            val typeDetails = TypeDetails(ExpressionNode.CollectionStatus.SINGLETON, name)
            return typeDetails
            TODO("not implemented")
        }

        override fun resolveValueSet(appContext: Any?, url: String?): ValueSet = TODO("not implemented")

    }
    engine.hostServices

    val result = engine.check(null, null, null, "Patient.name")

    println(result)

}