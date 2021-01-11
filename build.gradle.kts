plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
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
    version = "1.4.0"
}


subprojects {
    version = "1.4.0"
}

val ktor_version = "1.5.0"
val tomcat_version = "9.0.4"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation(group = "junit", name = "junit", version = "4.12")
    implementation(group = "org.nanohttpd", name = "nanohttpd", version = "2.2.0")
    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation(group = "commons-cli", name = "commons-cli", version = "1.4")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-client", version = "4.1.0")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-structures-r4", version = "4.1.0")
    implementation(group = "ca.uhn.hapi.fhir", name = "hapi-fhir-structures-dstu3", version = "4.1.0")
    implementation(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
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




