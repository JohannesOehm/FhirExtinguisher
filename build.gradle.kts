plugins {
    kotlin("jvm") version "1.3.60"
    antlr
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenCentral()
}

kotlin {
    group = "de.unimuenster.imi.fhir"
    version = "1.1"
}


subprojects {
    version = "1.0"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation(group = "junit", name = "junit", version = "4.12")
    implementation(group = "org.nanohttpd", name = "nanohttpd", version = "2.2.0")
    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation(group = "commons-cli", name = "commons-cli", version = "1.4")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-client", version = "4.1.0")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-structures-r4", version = "4.1.0")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-structures-dstu3", version = "4.1.0")
    implementation(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.60")
    antlr("org.antlr:antlr4:4.8")
}


val copyStaticPages by tasks.creating(Copy::class) {
    from("frontend/dist")
    into("$buildDir/resources/main/static")
    dependsOn(":frontend:webpack")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
        dependsOn(copyStaticPages, generateGrammarSource)
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        manifest {
            attributes("Main-Class" to "CLIKt")
        }
    }
    generateGrammarSource {
        arguments = arguments + listOf("-visitor")
    }
}




