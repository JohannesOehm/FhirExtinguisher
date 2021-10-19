plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    antlr
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("war")
//    id("com.bmuschko.tomcat") version "2.5"
}

war {
    webAppDirName = "webapp"
}

//tomcat {
//    contextPath = "/"
//    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
//    ajpProtocol = "org.apache.coyote.ajp.AjpNio2Protocol"
//}


repositories {
    mavenCentral()
    maven("https://jitpack.io") //Kotlin-ANTLR
}

kotlin {
    group = "de.unimuenster.imi.fhir"
    version = "1.7.2"
}


subprojects {
    version = "1.7.2"
}

val ktor_version = "1.6.1"
val tomcat_version = "9.0.4"
val hapi_version = "5.5.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    testImplementation("junit", "junit", "4.12")
    implementation("org.nanohttpd", "nanohttpd", "2.2.0")
    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation("commons-cli", "commons-cli", "1.4")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("ca.uhn.hapi.fhir", "hapi-fhir-client", hapi_version)
    implementation("ca.uhn.hapi.fhir", "hapi-fhir-structures-r4", hapi_version)
    implementation("ca.uhn.hapi.fhir", "hapi-fhir-structures-dstu3", hapi_version)
//    implementation("ca.uhn.hapi.fhir", "hapi-fhir-validation", "5.2.1")
//    implementation("ca.uhn.hapi.fhir", "hapi-fhir-validation-resources-r4", "5.2.1")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
    implementation("org.fhir:ucum:1.0.3")
    implementation("org.apache.commons", "commons-csv", "1.8")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-servlet:$ktor_version")
    antlr("org.antlr:antlr4:4.8")
//    tomcat("org.apache.tomcat.embed:tomcat-embed-core:$tomcat_version")
//    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat_version")
    implementation(project(":columns-parser"))
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
            attributes("Main-Class" to "fhirextinguisher.CLIKt")
        }
    }
    generateGrammarSource {
        arguments = arguments + listOf("-visitor")
    }
}




