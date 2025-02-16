import com.mockengine.plugin.util.runGitCommand

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
    signing
    id("plugin-gradle-publish")
    id("com.vanniktech.maven.publish")
}

version = runGitCommand(
    fileName = "version-name.txt",
    command = "git describe",
    default = "0.0.1",
)

dependencies {
    implementation(libs.plugin.jetbrains.extensions)
    implementation(libs.plugin.jetbrains.kotlin.plugin)
}

gradlePlugin {
    website = providers.gradleProperty("REPO_URL")
    vcsUrl = providers.gradleProperty("REPO_GIT_URL")

    plugins {
        create("gradlePlugin") {
            id = "io.github.pedro-bachiega.mock.engine"
            displayName = "MockEngineGradlePlugin"
            description = "\\o/"
            implementationClass = "com.pedrobneto.mock.engine.plugin.MockEngineGradlePlugin"
        }
    }
}
