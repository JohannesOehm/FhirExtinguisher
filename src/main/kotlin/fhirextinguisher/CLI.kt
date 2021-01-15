package fhirextinguisher

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options


private val log = KotlinLogging.logger {}

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
    val fhirServerUrl: String? = cmd.getOptionValue("fhirServer")?.dropLastWhile { it == '/' }
    val fhirVersion: String? = cmd.getOptionValue("fhirVersion")
    val authorization: String? = cmd.getOptionValue("authorization")
    val external: Boolean = cmd.hasOption("allowExternalConnection")

    if (portnumber == null) {
        println("Please provide a portnumber for the local server to start!")
        return
    }

    if (fhirServerUrl == null) {
        println("Please provide a URL for the FHIR server to connect!")
        return
    }


    val instanceConfiguration = InstanceConfigDTO(
        fhirVersion = fhirVersion,
        fhirServerUrl = fhirServerUrl,
        blockExternalRequests = !external,
        basicAuth = authorization,
        queryStorageFile = "savedQueries.csv"
    ).toInstanceConfiguration()

    embeddedServer(Netty, portnumber, module = application2(instanceConfiguration)).start(wait = true)
}
