plugins {
    kotlin("multiplatform")
}


val antlrKotlinVersion = "0ad2c42952"

buildscript {
    repositories {
        maven("https://jitpack.io")
    }

    dependencies {
        // add the plugin to the classpath
        classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:0ad2c42952")
    }
}

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
        browser {
        }
        binaries.library()
        compilations["main"].packageJson {
//            customField("name" to "columns-parser")
//            customField("version" to "1.0.0")
//            customField("main" to "columns-parser.js")
        }
    }


    sourceSets {
        val commonAntlr by creating {
            dependencies {
                api(kotlin("stdlib-common"))
                // add antlr-kotlin-runtime
                // otherwise, the generated sources will not compile
                api("com.strumenta.antlr-kotlin:antlr-kotlin-runtime:0ad2c42952")
                // antlr-kotlin-runtime-jvm is automatically added as an jvm dependency by gradle
            }
            // you have to add the generated sources the to the kotlin compiler source directory list
            // this is not required if you use src/commonAntlr/kotlin
            // and want to add the generated sources to version control
            kotlin.srcDir("build/generated-src/commonAntlr/kotlin")
        }


        val commonMain by getting {
            dependsOn(commonAntlr)
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
tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinCommonGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
        // antlr itself
        // antlr is transitive added by antlr-kotlin-target,
        // add another dependency if you want to choose another antlr4 version (not recommended)
        // project.dependencies.create("org.antlr:antlr4:$antlrVersion"),

        // antlr target, required to create kotlin code
        project.dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:0ad2c42952")
    )
    maxHeapSize = "64m"
    packageName = "com.strumenta.antlrkotlin.examples"
    arguments = listOf("-no-visitor", "-no-listener")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/commonAntlr/antlr").apply {
            include("*.g4")
        }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = File("build/generated-src/commonAntlr/kotlin")
    // use this settings if you want to add the generated sources to version control
    // outputDirectory = File("src/commonAntlr/kotlin")
}

tasks.getByName("compileKotlinJvm").dependsOn("generateKotlinCommonGrammarSource")
tasks.getByName("compileKotlinJs").dependsOn("generateKotlinCommonGrammarSource")