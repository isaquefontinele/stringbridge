plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    val samples = rootProject.layout.projectDirectory.dir("samples")
    inputs.dir(samples).withPropertyName("samples")
    systemProperty("stringbridge.samples", samples.asFile.absolutePath)
}
