package com.pedrobneto.mock.engine.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Locale

class MockEngineGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val hasKspApplied = extensions.findByName("ksp") != null
        if (hasKspApplied) {
            val kspExtension = extensions.findByName("ksp") ?: error("KSP config not found")
            val argMethod =
                kspExtension.javaClass.getMethod("arg", String::class.java, String::class.java)

            afterEvaluate {
                /**
                 * This is currently a workaround for a bug in KSP that causes the plugin
                 * to not work with multiplatform projects with only one target.
                 * https://github.com/google/ksp/issues/1525
                 */
                /**
                 * This is currently a workaround for a bug in KSP that causes the plugin
                 * to not work with multiplatform projects with only one target.
                 * https://github.com/google/ksp/issues/1525
                 */
                val singleTarget =
                    project.kotlinExtension.targets
                        .toList()
                        .size == 2

                if (kotlinExtension is KotlinMultiplatformExtension) {
                    if (singleTarget) {
                        argMethod.invoke(
                            kspExtension,
                            "MockEngine_MultiplatformWithSingleTarget",
                            "true"
                        )
                    } else {
                        tasks.withType(KotlinCompilationTask::class.java)
                            .configureEach { task ->
                                if (name.matches(Regex("(assemble|build).+"))) {
                                    task.dependsOn("kspCommonMainKotlinMetadata")
                                }
                            }
                    }
                }
            }

            val mockEngineKsp = "io.github.pedro-bachiega:mock-engine-processor"
            val dependency = "$mockEngineKsp:0.0.1"

            when (val kotlinExtension = kotlinExtension) {
                is KotlinSingleTargetExtension<*> -> {
                    dependencies.add("ksp", dependency)
                }

                is KotlinMultiplatformExtension -> {
                    kotlinExtension.targets.configureEach { target ->
                        if (target.platformType.name == "common") {
                            dependencies.add("kspCommonMainMetadata", dependency)
                            return@configureEach
                        }
                        val capitalizedTargetName = target.targetName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                            else it.toString()
                        }
                        dependencies.add("ksp$capitalizedTargetName", dependency)

                        if (target.compilations.any { it.name == "test" }) {
                            dependencies.add("ksp${capitalizedTargetName}Test", dependency)
                        }
                    }

                    kotlinExtension.sourceSets
                        .named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
                        .configure {
                            it.kotlin.srcDir(
                                "${layout.buildDirectory.get()}/generated/ksp/metadata/" +
                                        "${KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME}/kotlin"
                            )
                        }
                }

                else -> Unit
            }
        }
    }
}
