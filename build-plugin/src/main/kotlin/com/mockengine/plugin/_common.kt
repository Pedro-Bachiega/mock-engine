package com.mockengine.plugin

import com.android.build.api.dsl.CommonExtension
import com.mockengine.plugin.util.projectJavaVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun CommonExtension<*, *, *, *, *, *>.commonSetup() {
    buildFeatures.buildConfig = true

    androidResources { noCompress.add("") }

    compileOptions {
        sourceCompatibility(projectJavaVersion)
        targetCompatibility(projectJavaVersion)
    }

    packaging {
        resources.excludes.add("META-INF/LICENSE")
        resources.pickFirsts.add("protobuf.meta")
        jniLibs.keepDebugSymbols.addAll(setOf("*/mips/*.so", "*/mips64/*.so"))
    }
}

internal fun setupDefaultDependencies(kotlin: KotlinMultiplatformExtension) =
    kotlin.sourceSets.run {
        all {
            languageSettings {
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
    }
