import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask

buildscript {
    repositories {
        mavenCentral()
    }
}


plugins {
    kotlin("multiplatform")
    id("com.strumenta.antlr-kotlin") version "1.0.0"
}


val antlrKotlinVersion = "1.0.0"


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
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

tasks.getByName("compileKotlinJvm").dependsOn(generateKotlinCommonGrammarSource)
tasks.getByName("compileKotlinJs").dependsOn(generateKotlinCommonGrammarSource)