plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    signing
    `maven-publish`
    id("plugin-gradle-publish")
}

dependencies {
    implementation(project(":annotation"))

    // Google
    implementation(libs.google.ksp)
}
