import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("plugin-desktop-application")
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.mock.engine.gradle.plugin)
    alias(libs.plugins.ktorfit)
}

kotlin {
    sourceSets {
        val commonMain by getting
        commonMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)

            implementation(compose.uiTooling)

            implementation(libs.toolkit.arch.event.observer.core)
            implementation(libs.toolkit.arch.event.observer.compose)
            implementation(libs.toolkit.arch.splinter)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.jetbrains.compose.lifecycle.runtime)
            implementation(libs.jetbrains.compose.lifecycle.viewmodel)
            implementation(libs.jetbrains.coroutines.core)
            implementation(libs.jetbrains.coroutines.swing)
            implementation(libs.jetbrains.datetime)
            implementation(libs.jetbrains.serialization)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktorfit)

            implementation(project(":annotation"))
            implementation(project(":client"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.pedrobneto.mock.engine"
            packageVersion = "1.0.0"
        }
    }
}
