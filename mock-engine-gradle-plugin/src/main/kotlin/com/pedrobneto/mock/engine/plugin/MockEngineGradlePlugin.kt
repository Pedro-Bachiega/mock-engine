package com.pedrobneto.mock.engine.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import kotlin.reflect.full.declaredMemberProperties

class MockEngineGradlePlugin : Plugin<Project> {

    private val KotlinProjectExtension.targets: Iterable<KotlinTarget>
        get() = when (this) {
            is KotlinSingleTargetExtension<*> -> listOf(this.target)
            is KotlinMultiplatformExtension -> targets
            else -> error("Unexpected 'kotlin' extension $this")
        }

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
                val singleTarget = project.kotlinExtension.targets
                    .toList()
                    .size == 2

                if (kotlinExtension is KotlinMultiplatformExtension) {
                    if (singleTarget) {
                        argMethod.invoke(
                            kspExtension,
                            "MockEngine_MultiplatformWithSingleTarget",
                            true.toString()
                        )
                    } else {
                        val useKsp2 = kspExtension.javaClass.kotlin.declaredMemberProperties.find {
                            it.name == "useKsp2"
                        }?.call(kspExtension)?.let {
                            (it as? Property<*>?)?.get() as Boolean?
                        }
                            ?: project.findProperty("ksp.useKSP2")?.toString()?.toBoolean()
                            ?: false

                        if (useKsp2) {
                            tasks.named { name -> name.startsWith("ksp") }
                        } else {
                            tasks.withType(KotlinCompilationTask::class.java)
                        }.configureEach { task ->
                            if (task.name != "kspCommonMainKotlinMetadata") {
                                task.dependsOn("kspCommonMainKotlinMetadata")
                            }
                        }
                    }
                }
            }

            val dependency = "io.github.pedro-bachiega:mock-engine-processor:0.0.1-alpha02"
            when (val kotlinExtension = kotlinExtension) {
                is KotlinSingleTargetExtension<*> -> {
                    dependencies.add("ksp", dependency)
                }

                is KotlinMultiplatformExtension -> {
                    kotlinExtension.targets.configureEach { target ->
                        dependencies.add("kspCommonMainMetadata", dependency)
                    }

                    kotlinExtension.sourceSets
                        .named("commonMain")
                        .configure {
                            it.kotlin.srcDir(
                                "${layout.buildDirectory.get()}/generated/ksp/metadata/commonMain/kotlin"
                            )
                        }
                }
            }
        }
    }
}
