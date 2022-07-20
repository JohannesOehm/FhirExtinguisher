package fhirextinguisher

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options


private val log = KotlinLogging.logger {}

fun askUser(message: String): String {
    println(message)
    print("> ")
    return readLine()!!.substringAfter("> ")
}

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
        .addOption(
            "at",
            "authorizationToken",
            true,
            "Bearer Authorization required to connect to the FHIR server (if any)."
        )
        .addOption(
            Option(
                "h",
                "header",
                true,
                "Additional header to add to requests"
            ).apply { isRequired = false }
        )
        .addOption("p", "portNumber", true, "The port number for this server to use.")
        .addOption("t", "timeout", true, "The timeout in seconds for queries to the FHIR server (defaults to 60s)")
        .addOption(
            "ext",
            "allowExternalConnection",
            false,
            "Allow external (non-localhost) connections (might be a security issue)"
        )

    if (args.isEmpty() && System.getProperty("cli.mode") == "interactive") {
        println(
            """
___________.__    .__      ___________         __  .__                     .__       .__                  
\_   _____/|  |__ |__|_____\_   _____/__  ____/  |_|__| ____    ____  __ __|__| _____|  |__   ___________ 
 |    __)  |  |  \|  \_  __ \    __)_\  \/  /\   __\  |/    \  / ___\|  |  \  |/  ___/  |  \_/ __ \_  __ \
 |     \   |   Y  \  ||  | \/        \>    <  |  | |  |   |  \/ /_/  >  |  /  |\___ \|   Y  \  ___/|  | \/
 \___  /   |___|  /__||__| /_______  /__/\_ \ |__| |__|___|  /\___  /|____/|__/____  >___|  /\___  >__|   
     \/         \/                 \/      \/              \//_____/               \/     \/     \/       

"""
        )
        val serverUrl = askUser("Please enter the URL of your FHIR server: ")
        val authRequired = askUser("Does the server require authentication? [y/N]")
        val credentials = if (authRequired.isNotBlank() && authRequired == "y") {
            askUser("Please enter the Basic Auth credentials in the format username:password")
        } else null
        val instanceConfiguration = InstanceConfigDTO(
            fhirServerUrl = serverUrl,
            basicAuth = credentials
        ).toInstanceConfiguration()
        println("Application is running on port 8080! Open http://localhost:8080 in your browser! Press Ctrl+C in terminal to stop!")
        embeddedServer(Netty, 8080, module = application2(instanceConfiguration)).start(wait = true)


    }

    if (args.isEmpty()) {
        HelpFormatter().printHelp("FhirExtinguisher", options)
        return
    }
    val cmd = DefaultParser().parse(options, args)

    val portnumber: Int? = cmd.getOptionValue("portNumber")?.toInt()
    val fhirServerUrl: String? = cmd.getOptionValue("fhirServer")?.dropLastWhile { it == '/' }
    val fhirVersion: String? = cmd.getOptionValue("fhirVersion")
    val authorization: String? = cmd.getOptionValue("authorization")
    val authorizationToken: String? = cmd.getOptionValue("authorizationToken")
    val headers: Array<String>? = cmd.getOptionValues("header")
    val timeoutstr: String? = cmd.getOptionValue("timeout")
    val external: Boolean = cmd.hasOption("allowExternalConnection")

    if (portnumber == null) {
        println("Please provide a portnumber for the local server to start!")
        return
    }

    if (fhirServerUrl == null) {
        println("Please provide a URL for the FHIR server to connect!")
        return
    }

    val timeout = if (timeoutstr == null) {
        60_000
    } else {
        if (timeoutstr.toIntOrNull() == null) {
            println("Please provide valid integer as timeout!")
            return
        } else {
            timeoutstr.toInt() * 1000
        }
    }

    val instanceConfiguration = InstanceConfigDTO(
        fhirVersion = fhirVersion,
        fhirServerUrl = fhirServerUrl,
        blockExternalRequests = !external,
        basicAuth = authorization,
        tokenAuth = authorizationToken,
        queryStorageFile = "savedQueries.csv",
        headers = headers?.toList() ?: emptyList(),
        timeoutInMillis = timeout
    ).toInstanceConfiguration()

    embeddedServer(Netty, portnumber, module = application2(instanceConfiguration)).start(wait = true)
}
