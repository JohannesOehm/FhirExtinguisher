import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IClientInterceptor
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import fi.iki.elonen.util.ServerRunner
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options

fun main(args: Array<String>) {
    val options = Options()
        .addOption("f", "fhirServer", true, "The endpoint URL of the FHIR server to call.")
        .addOption(
            "v",
            "fhirVersion",
            true,
            "The FHIR (D)STU/R version to use. Must be either DSTU2, DSTU3, R4 or R5"
        )
        .addOption(
            "a",
            "authorization",
            true,
            "Basic Authorization required to connect to the FHIR server (if any). \"username:password\"!"
        )
        .addOption("p", "portNumber", true, "The port number for this server to use.")

    if (args.size == 0) {
        HelpFormatter().printHelp("FhirExtinguisher", options)
        return
    }

    val cmd = DefaultParser().parse(options, args)

    val portnumber = cmd.getOptionValue("portNumber").toInt()
    val fhirServerUrl = cmd.getOptionValue("fhirServer").dropLastWhile { it == '/' }
    val fhirVersion: String? = cmd.getOptionValue("fhirVersion")
    val authorization: String? = cmd.getOptionValue("authorization")


    val fhirContext = if (fhirVersion != null) {
        if (fhirVersion == "r4") {
            FhirContext.forR4()
        } else if (fhirVersion == "stu3" || fhirVersion == "dstu3") {
            FhirContext.forDstu3()
        } else {
            FhirContext.forR4()
        }
    } else {
        FhirContext.forR4()
    }

    val interceptors = if (authorization != null) {
        val (username, passwd) = authorization.split(':', limit = 2)
        listOf(BasicAuthInterceptor(username, passwd))
    } else emptyList<IClientInterceptor>()


    //TODO: Validate FHIR server URL

    val fhirExtinguisher = FhirExtinguisher(portnumber, fhirServerUrl, fhirContext, interceptors)

    ServerRunner.executeInstance(fhirExtinguisher)
}