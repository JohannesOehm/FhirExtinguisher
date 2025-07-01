plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "FhirExtinguisher"
include("frontend")
include("columns-parser")
include("transform-fhir")
