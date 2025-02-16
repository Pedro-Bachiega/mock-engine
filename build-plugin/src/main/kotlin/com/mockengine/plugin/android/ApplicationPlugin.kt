package com.mockengine.plugin.android

import com.android.build.api.dsl.ApplicationExtension
import com.mockengine.plugin.commonSetup
import com.mockengine.plugin.util.androidApplication
import com.mockengine.plugin.util.applyPlugins
import com.mockengine.plugin.util.kotlinAndroid
import com.mockengine.plugin.util.libs
import com.mockengine.plugin.util.projectJavaTarget
import com.mockengine.plugin.util.projectJavaVersionCode
import com.mockengine.plugin.util.version
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class ApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        applyPlugins(
            "android-application",
            "jetbrains-kotlin-android",
            "jetbrains-compose-compiler",
            "jetbrains-compose-kotlin",
            "jetbrains-serialization",
        )

        val kotlin = kotlinAndroid ?: return@with
        val android = androidApplication ?: return@with

        kotlin.jvmToolchain(projectJavaVersionCode)
        kotlin.compilerOptions { jvmTarget.set(projectJavaTarget) }

        setup(android)

        plugins.apply("plugin-lint")
        plugins.apply("plugin-test")
        plugins.apply("plugin-optimize")
    }

    private fun Project.setup(android: ApplicationExtension) = with(android) {
        // Exclusive Application Configurations
        compileSdk = libs.version("build-sdk-compile").toInt()
        buildToolsVersion = libs.version("build-tools")

        defaultConfig {
            minSdk = libs.version("build-sdk-min-sample").toInt()
            targetSdk = libs.version("build-sdk-target").toInt()

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            versionCode = libs.version("build-version-code").toInt()
            versionName = libs.version("build-version-name")

            resourceConfigurations.add("en")
            resourceConfigurations.add("pt")
        }

        // TODO Config json for buildTypes and flavors
        buildTypes.maybeCreate("release").apply { isMinifyEnabled = false }
        buildTypes.maybeCreate("debug").apply {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }

        // Common Setup
        commonSetup()

        sourceSets {
            maybeCreate("main").java.srcDirs("src/main/kotlin")
            maybeCreate("test").java.srcDirs("src/test/kotlin")
            maybeCreate("androidTest").java.srcDirs("src/androidTest/kotlin")
            maybeCreate("androidTest").resources.srcDirs("src/androidTest/res")
        }
    }
}
