import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

buildscript {
    repositories {
        mavenCentral()
    }
}

group = "de.unimuenster.imi.fhir"
version = "1.7.8"

plugins {
    kotlin("multiplatform")
    id("com.strumenta.antlr-kotlin") version "1.0.0"
    id("maven-publish")
}


val antlrKotlinVersion = "1.0.0"


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        binaries.library()
        browser {
            webpackTask {
                output.libraryTarget = "commonjs" // "commonjs", "module", "var", etc.
            }
        }
        compilations["main"].packageJson {
//            customField("name" to "columns-parser")
//            customField("version" to "1.0.0")
//            customField("main" to "columns-parser.js")
        }
    }


    sourceSets {
        commonMain {
            dependencies {
                implementation("com.strumenta:antlr-kotlin-runtime:1.0.0")
                kotlin {
                    srcDir(layout.buildDirectory.dir("generatedAntlr"))
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}


// in antlr-kotlin-plugin <0.0.5, the configuration was applied by the plugin.
// starting from verison 0.0.5, you have to apply it manually:
val generateKotlinCommonGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    maxHeapSize = "64m"
    val _packageName = "com.strumenta.antlrkotlin.examples"
    packageName = _packageName
    arguments = listOf("-no-visitor", "-no-listener")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/commonAntlr/antlr").apply {
            include("*.g4")
        }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = layout.buildDirectory.dir(
        "generatedAntlr/${_packageName.replace(".", "/")}"
    ).get().asFile
    // use this settings if you want to add the generated sources to version control
    // outputDirectory = File("src/commonAntlr/kotlin")
}


tasks.withType<KotlinCompileCommon> {
    dependsOn(generateKotlinCommonGrammarSource)
    inputs.files(generateKotlinCommonGrammarSource.get().outputs.files)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateKotlinCommonGrammarSource)
    inputs.files(generateKotlinCommonGrammarSource.get().outputs.files)
}

tasks.getByName("compileKotlinJvm").dependsOn(generateKotlinCommonGrammarSource)
tasks.getByName("compileKotlinJs").dependsOn(generateKotlinCommonGrammarSource)
tasks.getByName("jsSourcesJar").dependsOn(generateKotlinCommonGrammarSource)
tasks.getByName("jvmSourcesJar").dependsOn(generateKotlinCommonGrammarSource)
tasks.getByName("sourcesJar").dependsOn(generateKotlinCommonGrammarSource)


publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("FhirExtinguisherColumnsParser")
                description.set("A module for transforming data into tabular format")
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