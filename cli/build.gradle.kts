plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.clikt)
    testImplementation(kotlin("test"))
}

application {
    applicationName = "stringbridge"
    mainClass = "dev.stringbridge.cli.MainKt"
}

tasks.test {
    useJUnitPlatform()
}
