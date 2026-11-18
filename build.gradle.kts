plugins {
    kotlin("jvm") version "2.4.20"
}

group = "org.fomin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val camelVersion = "4.22.1"
val exposedVersion = "1.5.0"

dependencies {
    // Apache Camel
    implementation("org.apache.camel:camel-main:$camelVersion")
    implementation("org.apache.camel:camel-http:$camelVersion")
    implementation("org.apache.camel:camel-direct:${camelVersion}")
    implementation("org.apache.camel:camel-timer:${camelVersion}")
    implementation("org.apache.camel:camel-jackson:$camelVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.camel:camel-csv:$camelVersion")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.7.8")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}