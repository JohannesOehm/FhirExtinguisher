import org.panteleyev.jpackage.JPackageTask

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("antlr")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("war")
    id("org.panteleyev.jpackageplugin") version "1.6.0"
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

val projectVersion: String by project

kotlin {
    group = "de.unimuenster.imi.fhir"
    version = projectVersion
}


subprojects {
    version = projectVersion
}

val ktor_version = "2.3.5"
val tomcat_version = "9.0.4"
val hapi_version: String by project

dependencies {
    implementation(project("columns-parser"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-servlet-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-servlet-jakarta:$ktor_version")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    testImplementation("junit:junit:4.13.1")
//    implementation("org.nanohttpd:nanohttpd:2.2.0")
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
    antlr("org.antlr:antlr4:4.13.2")
//    tomcat("org.apache.tomcat.embed:tomcat-embed-core:$tomcat_version")
//    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:$tomcat_version")
    implementation(project(":transform-fhir"))
    implementation(project(":columns-parser"))
}


val copyStaticPages by tasks.creating(Copy::class) {
    from("frontend/dist")
    into(layout.buildDirectory.dir("resources/main/static"))
    dependsOn(":frontend:webpack")
}

tasks.getByName("war"){
    dependsOn(copyStaticPages)
}

kotlin {
    jvmToolchain(17)
}

tasks {
    compileKotlin {
        dependsOn(copyStaticPages, generateGrammarSource)
    }
    jar {
        manifest {
            attributes("Main-Class" to "fhirextinguisher.CLIKt")
        }
    }
    shadowJar {
//        mainClass = "fhirextinguisher.CLIKt"
    }
    generateGrammarSource {
        arguments = arguments + listOf("-visitor")
    }
}


//FOLLOWING TASKS CREATE SYSTEM DEPENDENT BINARY WITH JRE
task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("${layout.buildDirectory.get()}/jars")
}

task("copyJar", Copy::class) {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar).into("${layout.buildDirectory.get()}/jars")
}



tasks.register<JPackageTask>("CreateAppImage") {
    dependsOn("copyJar")

    input = "${layout.buildDirectory.get()}/jars"
    destination = "${layout.buildDirectory.get()}/dist"

    appName = "FhirExtinguisher"

    mainJar = tasks.shadowJar.get().archiveFileName.get()
    mainClass = "fhirextinguisher.CLIKt"

    javaOptions = listOf("-Dfile.encoding=UTF-8", "-Dcli.mode=interactive")
    type = org.panteleyev.jpackage.ImageType.APP_IMAGE

    winConsole = true
}

tasks.register<JPackageTask>("CreateEXE") {
    dependsOn("copyJar")

    input = "${layout.buildDirectory.get()}/jars"
    destination = "${layout.buildDirectory.get()}/dist"

    appName = "FhirExtinguisher"

    mainJar = tasks.shadowJar.get().archiveFileName.get()
    mainClass = "fhirextinguisher.CLIKt"

    javaOptions = listOf("-Dfile.encoding=UTF-8", "-Dcli.mode=interactive")
    type = org.panteleyev.jpackage.ImageType.EXE

    winDirChooser = true
    winMenu = true
    winConsole = true
}

allprojects {
    apply(plugin = "maven-publish")
}

tasks.register("publishAllModules") {
    dependsOn(":transform-fhir:publish", ":columns-parser:publish")
}


