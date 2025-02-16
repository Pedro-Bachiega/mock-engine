package com.mockengine.plugin.desktop

import com.mockengine.plugin.util.applyPlugins
import com.mockengine.plugin.util.kotlinMultiplatform
import com.mockengine.plugin.util.projectJavaVersionCode
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal class ApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        applyPlugins(
            "jetbrains-kotlin-multiplatform",
            "jetbrains-compose-compiler",
            "jetbrains-compose-kotlin",
        )

        kotlinExtension.jvmToolchain(projectJavaVersionCode)
        kotlinMultiplatform?.run {
            applyDefaultHierarchyTemplate()
            jvm()
        }

        plugins.apply("plugin-lint")
        plugins.apply("plugin-optimize")
    }
}
