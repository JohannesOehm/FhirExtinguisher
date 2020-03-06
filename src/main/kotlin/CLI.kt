import ca.uhn.fhir.context.FhirContext
import fi.iki.elonen.util.ServerRunner
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor
import ca.uhn.fhir.rest.client.api.IClientInterceptor

fun main(args: Array<String>) {
    val options = Options()
        .addOption("f", "fhirServer", true, "The endpoint URL of the FHIR server to call.")
        .addOption(
            "v",
            "fhirVersion",
            true,
            "The FHIR (D)STU/R version to use. Must be either dstu3 or r4."
        )
        .addOption(
            "a",
            "authorization",
            true,
            "Basic Authorization required to connect to the FHIR server (if any). \"username:password\"!"
        )
        .addOption("p", "portNumber", true, "The port number for this server to use.")
        .addOption(
            "ext",
            "allowExternalConnection",
            false,
            "Allow external (non-localhost) connections (might be a security issue)"
        )

    if (args.isEmpty()) {
        HelpFormatter().printHelp("FhirExtinguisher", options)
        return
    }

    val cmd = DefaultParser().parse(options, args)

    val portnumber: Int? = cmd.getOptionValue("portNumber")?.toInt()
    val fhirServerUrl: String? = cmd.getOptionValue("fhirServer").dropLastWhile { it == '/' }
    val fhirVersion: String? = cmd.getOptionValue("fhirVersion")
    val authorization: String? = cmd.getOptionValue("authorization")
    val external: Boolean = cmd.hasOption("allowExternalConnection")

    if (portnumber == null) {
        println("Please provide a portnumber for the local server to start")
        return
    }

    if (fhirServerUrl == null) {
        println("Please provide a URL for the FHIR server to connect")
        return
    }



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

    val (interceptors, basicAuth) = if (authorization != null) {
        val (username, passwd) = authorization.split(':', limit = 2)
        listOf(BasicAuthInterceptor(username, passwd)) to BasicAuthData(username, passwd)
    } else emptyList<IClientInterceptor>() to null


    //TODO: Validate FHIR server URL

    val instanceConfiguration =
        InstanceConfiguration(fhirServerUrl, fhirContext, basicAuth, interceptors, !external)
    val fhirExtinguisher = MyRouters(portnumber, instanceConfiguration)

    ServerRunner.executeInstance(fhirExtinguisher)
}