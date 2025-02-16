package com.mockengine.plugin.util

import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

internal fun Project.setupJavadocAndSources() {
    setupSources()
    setupJavadoc()
}

private fun Project.setupJavadoc() {
    applyPlugins("jetbrains-dokka")
    tasks.register("javadocJar", Jar::class.java) {
        group = "documentation"
        val dokka = project.tasks.named("dokkaHtml", DokkaTask::class.java)
        dependsOn(dokka)
        from(dokka.flatMap(DokkaTask::outputDirectory))
        archiveClassifier.set("javadoc")
        archiveFileName.set("${project.name}-release-javadoc.jar")
    }
}

private fun Project.setupSources() {
    tasks.whenTaskAdded {
        if (this !is Jar) return@whenTaskAdded
        when (name) {
            "releaseSourcesJar" -> archiveFileName.set("${project.name}-release-sources.jar")
            "debugSourcesJar" -> archiveFileName.set("${project.name}-debug-sources.jar")
        }
    }
}
