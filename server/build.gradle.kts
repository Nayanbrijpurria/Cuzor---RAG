plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.rpc)
}


application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.kotlinx.rpc.server)
    implementation(libs.logback.classic)
    implementation(project(":core"))

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.kotlinx.rpc.client)

    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.3")

// Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")

// Hikari Connection Pool
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("com.opencsv:opencsv:5.9")

    implementation("dev.langchain4j:langchain4j:1.0.1")

    implementation("com.microsoft.onnxruntime:onnxruntime:1.20.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("com.knuddels:jtokkit:1.1.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")


    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")



}
