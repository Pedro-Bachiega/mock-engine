package com.mockengine.plugin

import com.mockengine.plugin.util.androidApplication
import com.mockengine.plugin.util.androidLibrary
import com.mockengine.plugin.util.applyPlugins
import com.mockengine.plugin.util.kotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class ComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        applyPlugins(
            "jetbrains-compose-compiler",
            "jetbrains-compose-kotlin",
        )

        kotlinMultiplatform?.let(::setupDefaultDependencies)
        (androidApplication ?: androidLibrary)?.run { buildFeatures.compose = true }
            ?: return@with
    }
}
