package fhirextinguisher

import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.fhirpath.ExpressionNode
import org.hl7.fhir.r4.fhirpath.FHIRPathEngine
import org.hl7.fhir.r4.fhirpath.FHIRPathUtilityClasses
import org.hl7.fhir.r4.fhirpath.TypeDetails
import org.hl7.fhir.r4.model.Base
import org.hl7.fhir.r4.model.ValueSet

fun main() {

    val engine = FHIRPathEngine(SimpleWorkerContext())
    engine.hostServices = object : FHIRPathEngine.IEvaluationContext {
        override fun resolveFunction(
            engine: FHIRPathEngine?,
            functionName: String?
        ): FHIRPathUtilityClasses.FunctionDetails? {
            return null
        }

        override fun executeFunction(
            engine: FHIRPathEngine?,
            appContext: Any?,
            focus: MutableList<Base>?,
            functionName: String?,
            parameters: MutableList<MutableList<Base>>?
        ): MutableList<Base>? {
            return mutableListOf<Base>()
        }

        override fun checkFunction(
            engine: FHIRPathEngine?,
            appContext: Any?,
            functionName: String?,
            focus: TypeDetails?,
            parameters: MutableList<TypeDetails>?
        ): TypeDetails? =
            TODO("not implemented")

        override fun resolveConstant(
            engine: FHIRPathEngine?,
            appContext: Any?,
            name: String?,
            beforeContext: Boolean,
            explicitConstant: Boolean
        ): MutableList<Base>? =
            TODO("not implemented")

        override fun log(argument: String?, focus: MutableList<Base>?): Boolean {
            return true
        }

        override fun resolveReference(
            engine: FHIRPathEngine?,
            appContext: Any?,
            url: String?,
            refContext: Base?
        ): Base? = TODO()

        override fun conformsToProfile(engine: FHIRPathEngine?, appContext: Any?, item: Base?, url: String?): Boolean =
            TODO("not implemented")

        override fun resolveConstantType(
            engine: FHIRPathEngine?,
            appContext: Any?,
            name: String?,
            explicitConstant: Boolean
        ): TypeDetails? {
            println("resolveConstantType(appContext=$appContext, name=$name)")
            val typeDetails = TypeDetails(ExpressionNode.CollectionStatus.SINGLETON, name)
            return typeDetails
            TODO("not implemented")
        }

        override fun resolveValueSet(engine: FHIRPathEngine?, appContext: Any?, url: String?): ValueSet? =
            TODO("not implemented")

    }
    engine.hostServices

    val result = engine.check(null, null, null, "Patient.name")

    println(result)

}