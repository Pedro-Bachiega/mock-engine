package com.mockengine.plugin.multiplatform

import com.android.build.api.dsl.LibraryExtension
import com.mockengine.plugin.commonSetup
import com.mockengine.plugin.util.androidLibrary
import com.mockengine.plugin.util.applyPlugins
import com.mockengine.plugin.util.kotlinMultiplatform
import com.mockengine.plugin.util.libs
import com.mockengine.plugin.util.projectJavaTarget
import com.mockengine.plugin.util.projectJavaVersionCode
import com.mockengine.plugin.util.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

@OptIn(ExperimentalWasmDsl::class)
internal class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        applyPlugins(
            "android-library",
            "jetbrains-kotlin-multiplatform",
            "jetbrains-serialization",
        )

        kotlinExtension.jvmToolchain(projectJavaVersionCode)

        val android = androidLibrary ?: return@with
        val kotlin = kotlinMultiplatform ?: return@with
        setupAndroid(android, kotlin)
        kotlin.setupTargets()

        plugins.apply("plugin-lint")
        plugins.apply("plugin-optimize")
    }

    private fun Project.setupAndroid(
        android: LibraryExtension,
        kotlin: KotlinMultiplatformExtension
    ) = with(android) {
        kotlin.androidTarget().compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(projectJavaTarget)
            }
        }

        // Common Setup
        commonSetup()

        compileSdk = libs.version("build-sdk-compile").toInt()
        buildToolsVersion = libs.version("build-tools")

        // Exclusive Library Configurations
        defaultConfig {
            minSdk = libs.version("build-sdk-min-toolkit").toInt()

            consumerProguardFiles("consumer-proguard-rules.pro")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        sourceSets {
            maybeCreate("main").apply {
                java.srcDirs("src/androidMain/kotlin")
                res.srcDirs("src/androidMain/res")
                resources.srcDirs("src/commonMain/resources")
            }
            maybeCreate("test").java.srcDirs("src/test/kotlin")
            maybeCreate("androidTest").java.srcDirs("src/androidTest/kotlin")
            maybeCreate("androidTest").resources.srcDirs("src/androidTest/res")
        }
    }

    private fun KotlinMultiplatformExtension.setupTargets() {
//        wasmJs()
//        wasmWasi()

        jvm()
//        js(IR) {
//            this.nodejs()
//            binaries.executable() // not applicable to BOTH, see details below
//        }
        androidTarget {
            publishLibraryVariants("release", "debug")
        }
//        androidNativeArm32()
//        androidNativeArm64()
//        androidNativeX86()
//        androidNativeX64()

//        iosArm64()
//        iosX64()
//        iosSimulatorArm64()
//        watchosArm32()
//        watchosArm64()
//        watchosX64()
//        watchosSimulatorArm64()
//        tvosArm64()
//        tvosX64()
//        tvosSimulatorArm64()
//        macosX64()
//        macosArm64()
//        linuxX64 {
//            binaries {
//                executable()
//            }
//        }
//        linuxArm64 {
//            binaries {
//                executable()
//            }
//        }
//        listOf(
//            iosX64(),
//            iosArm64(),
//            iosSimulatorArm64(),
//            watchosArm32(),
//            watchosArm64(),
//            watchosSimulatorArm64(),
//            tvosArm64(),
//            tvosX64(),
//            tvosSimulatorArm64(),
//        ).forEach {
//            it.binaries.framework { baseName = "library" }
//        }
//
//        mingwX64()
        applyDefaultHierarchyTemplate()
    }
}
