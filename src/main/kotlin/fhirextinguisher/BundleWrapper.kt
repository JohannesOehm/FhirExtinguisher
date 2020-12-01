package fhirextinguisher

import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition
import org.hl7.fhir.instance.model.api.IBase
import java.beans.Introspector
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class HapiWrapper(val value: IBase)

class BundleWrapper(private val definition: BaseRuntimeElementCompositeDefinition<*>, value: IBase) :
    HapiWrapper(value) {
    val link: List<BundleLinkComponentWrapper>
        get() {
            val definition = definition.getChildByName("link")
            return definition.accessor.getValues(value).map { BundleLinkComponentWrapper(it) }
        }

    val entry: List<BundleEntryComponentWrapper>
        get() {
            val definition = definition.getChildByName("entry")
            return definition.accessor.getValues(value).map { BundleEntryComponentWrapper(it) }
        }

}

class BundleEntryComponentWrapper(value: IBase) : HapiWrapper(value) {
    val resource: IBase? by ReflectionDelegate("getResource")
}

class BundleLinkComponentWrapper(value: IBase) : HapiWrapper(value) {
    val url: String? by ReflectionDelegate()
    val relation: String? by ReflectionDelegate()
}

class ReflectionDelegate<T>(private val getterName: String? = null) : ReadOnlyProperty<HapiWrapper, T?> {
    override fun getValue(thisRef: HapiWrapper, property: KProperty<*>): T? {
        if (getterName != null) { //Because BundleEntry has getResource() and isResource()
            return thisRef.value.javaClass.getMethod(getterName).invoke(thisRef.value) as T
        } else {
            val beanInfo = Introspector.getBeanInfo(thisRef.value.javaClass)
            val find = beanInfo.propertyDescriptors.find { it.name == property.name }
            return find?.readMethod?.invoke(thisRef.value) as T
        }

    }
}