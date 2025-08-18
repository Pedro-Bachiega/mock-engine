plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
}

apply(from = "$rootDir/gradle/versioning.gradle.kts")

dependencies {
    implementation(libs.plugin.jetbrains.extensions)
    implementation(libs.plugin.jetbrains.kotlin.plugin)
}

gradlePlugin {
    website = providers.gradleProperty("POM_URL")
    vcsUrl = providers.gradleProperty("POM_SCM_CONNECTION")

    plugins {
        create("gradlePlugin") {
            id = "io.github.pedro-bachiega.mock.engine"
            displayName = "MockEngineGradlePlugin"
            description = "\\o/"
            implementationClass = "com.pedrobneto.mock.engine.plugin.MockEngineGradlePlugin"
        }
    }
}
