package wrappers

import org.hl7.fhir.instance.model.api.IPrimitiveType
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Enumeration
import java.util.*

/**
 * Collection of human-readable .toString()-functions for the complex FHIR types
 */
object ToStringHelperR4 {
    fun convertToString(it: Base): String =
        when (it) {
            is Enumeration<*> -> it.code
            is Quantity -> getQuantityAsString(it)
            is DecimalType -> Objects.toString(it.value)
            is CodeableConcept -> getCodeableConceptAsString(it)
            is IPrimitiveType<*> -> it.valueAsString
            is HumanName -> getNameAsString(it)
            is Identifier -> getIdentifierAsString(it)
            is Reference -> getReferenceAsString(it)
            is Attachment -> getAttachmentAsString(it)
            is Coding -> getCodingAsString(it)
            is Money -> getMoneyAsString(it)
            is Range -> getRangeAsString(it)
            is Ratio -> getRatioAsString(it)
            is Period -> getPeriodAsString(it)
            is SampledData -> getSampledDataAsString(it)
            is Address -> getAddressAsString(it)
            is ContactPoint -> getContactPointAsString(it)
            is Timing -> getTimingAsString(it)
            is Signature -> getSignatureAsString(it)
            is org.hl7.fhir.r4.model.Annotation -> getAnnotationAsString(it)
            else -> it.toString()
        }

    private fun getSignatureAsString(it: Signature): String {
        return "Signature[" + it.type.joinToString { getCodingAsString(it) } + "]"
    }

    private fun getAnnotationAsString(it: org.hl7.fhir.r4.model.Annotation): String {
        return it.text
    }

    private fun getContactPointAsString(it: ContactPoint): String {
        val sb = StringBuilder()
        if (it.hasSystem()) {
            sb.append(it.system).append(": ")
        }
        sb.append(it.value)
        if (it.hasUse()) {
            sb.append(" (" + it.use + ")")
        }
        if (it.hasRank()) {
            sb.append("[rank=").append(it.rank).append("]")
        }
        if (it.hasPeriod()) {
            sb.append(" ").append(getPeriodAsString(it.period))
        }
        return sb.toString()
    }

    private fun getTimingAsString(it: Timing): String {
        return "Timing" //TODO
    }

    private fun getAddressAsString(it: Address): String {
        if (it.hasText()) {
            return it.text
        }
        val sb = StringBuilder()
        val street = it.line.joinToString("\n")
        sb.append(street)
        if (it.hasPostalCode() || it.hasCity()) {
            sb.append(it.postalCode).append(" ").append(it.city).append("\n")
        }

        //TODO: No idea in which order to put these
        if (it.hasDistrict()) {
            sb.append(it.district).append("\n")
        }
        if (it.hasState()) {
            sb.append(it.state).append("\n")
        }
        if (it.hasCountry()) {
            sb.append(it.country).append("\n")
        }
        if (it.hasUse() || it.hasType() || it.hasPeriod()) {
            sb.append("(" + it.use + ", " + it.type + ", " + getPeriodAsString(it.period) + ")")
        }
        return sb.toString()
    }

    private fun getQuantityAsString(it: Quantity) = ("${it.value} ${it.unit}")

    private fun getRangeAsString(it: Range): String {
        return if (it.low != null) getQuantityAsString(it.low) else "" +
                " .. " +
                if (it.high != null) getQuantityAsString(it.high) else ""
    }

    private fun getRatioAsString(it: Ratio): String {
        return if (it.numerator != null) getQuantityAsString(it.numerator) else "" +
                " / " +
                if (it.denominator != null) getQuantityAsString(it.denominator) else ""
    }

    private fun getPeriodAsString(it: Period): String {
        return "${it.start} .. ${it.end}"
    }

    private fun getMoneyAsString(it: Money): String {
        val sb = StringBuilder();
        sb.append(it.value)

        if (it.hasCurrency()) {
            sb.append(" " + it.currency)
        }
        return sb.toString()
    }

    private fun getAttachmentAsString(it: Attachment): String {
        val sb = StringBuilder("Attachment")
        if (it.hasContentType() && it.hasUrl()) {
            sb.append("[" + it.contentType + "," + it.url)
        } else if (it.hasContentType()) {
            sb.append("[" + it.contentType + "]")
        } else if (it.hasUrl()) {
            sb.append("[" + it.url + "]")
        }
        if (it.hasTitle()) {
            sb.append(" " + it.title)
        }
        return sb.toString()
    }

    private fun getSampledDataAsString(it: SampledData): String {
        return "SampledData"
    }


    private fun getCodingAsString(it: Coding): String {
        val sb = StringBuilder()
        if (it.hasSystem()) {
            sb.append(it.system).append('|')
        }
        if (it.hasVersion()) {
            sb.append(it.version).append('|')
        }
        sb.append(it.code)
        return sb.toString()

    }

    private fun getCodeableConceptAsString(it: CodeableConcept) =
        if (it.text != null) it.text else it.coding.joinToString { getCodingAsString(it) }

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
            " (${getCodeableConceptAsString(identifier.type)}, ${identifier.use.toCode()})"
        } else if (identifier.hasUse()) {
            " (${identifier.use.toCode()})"
        } else if (identifier.hasType()) {
            " (${getCodeableConceptAsString(identifier.type)})"
        } else {
            ""
        }
        val period = if (identifier.hasPeriod()) " [" + getPeriodAsString(identifier.period) + "]" else ""
        val assigner =
            if (identifier.hasAssigner()) " {" + getReferenceAsString(identifier.assigner) + "}" else ""
        return idPart + bracketPart + period + assigner
    }
}