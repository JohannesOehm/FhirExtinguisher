import fi.iki.elonen.util.ServerRunner
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options

fun main(args: Array<String>) {
    val options = Options()
        .addOption("f", "fhirServer", true, "The endpoint URL of the FHIR server to call.")
        .addOption("v", "fhirVersion", false, "The FHIR (D)STU/R version to use. Must be either DSTU2, DSTU3, R4 or R5")
        .addOption("p", "portNumber", true, "The port number for this server to use.")

    if (args.size == 0) {
        HelpFormatter().printHelp("FhirExtinguisher", options)
        return
    }

    val cmd = DefaultParser().parse(options, args)

    val portnumber = cmd.getOptionValue("portNumber").toInt()
    val fhirServerUrl = cmd.getOptionValue("fhirServer").dropLastWhile { it == '/' }
    val fhirVersion = cmd.getOptionValue("fhirVersion")
    val fhirExtinguisher = FhirExtinguisher(portnumber, fhirServerUrl)

    ServerRunner.executeInstance(fhirExtinguisher)


}