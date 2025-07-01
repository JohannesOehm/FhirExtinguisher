plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("maven-publish")
}

group = "de.unimuenster.imi.fhir"
version = "1.7.8"

repositories {
    mavenCentral()
}

val hapi_version: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:$hapi_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapi_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-dstu3:$hapi_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:$hapi_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:$hapi_version")
//    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("org.fhir:ucum:1.0.9")
    implementation("org.apache.commons:commons-csv:1.13.0")
    implementation(project(":columns-parser"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("FhirExtinguisherCsvTransformer")
                description.set("A module for transforming FHIR data into CSV")
                url.set("https://github.com/JohannesOehm/FhirExtinguisher")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/JohannesOehm/FhirExtinguisher")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "unknown"
                password = System.getenv("GITHUB_TOKEN") ?: "unknown"
            }
        }
    }
}