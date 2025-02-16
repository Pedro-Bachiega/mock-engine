package com.mockengine.plugin.multiplatform

import com.mockengine.plugin.util.attachAllTasksIntoAssembleRelease
import com.mockengine.plugin.util.configurePom
import com.mockengine.plugin.util.createLocalPathRepository
import com.mockengine.plugin.util.createSonatypeRepository
import com.mockengine.plugin.util.kotlinMultiplatform
import com.mockengine.plugin.util.publishing
import com.mockengine.plugin.util.requireAll
import com.mockengine.plugin.util.setupJavadocAndSources
import com.mockengine.plugin.util.setupSigning
import com.mockengine.plugin.util.versionName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.konan.file.File

internal class PublishPlugin : Plugin<Project> {

    private val Project.javadoc: String?
        get() = "$projectDir/build/libs/$name-release-javadoc.jar".takeIf { File(it).exists }

    override fun apply(target: Project) {
        target.requireAll(
            "plugin-multiplatform-publish",
            "plugin-multiplatform-library",
        )
        target.plugins.apply("maven-publish")

        target.kotlinMultiplatform?.run {
            withSourcesJar(true)
            androidTarget().publishLibraryVariants("release")
        } ?: return

        // Setup Javadoc and sources artifacts
        target.setupJavadocAndSources()

        // Setup Publishing
        with(target.publishing) {
            repositories {
                createLocalPathRepository(target)
                createSonatypeRepository(target)
            }

            publications {
                withType(MavenPublication::class.java) {
                    val suffix = when {
                        name.contains("android") -> "-android"
                        name.contains("jvm") -> "-jvm"
                        else -> ""
                    }
                    groupId = target.properties["GROUP_ID"] as String
                    artifactId = "mock-engine-${target.name}$suffix"
                    version = target.versionName

                    target.javadoc?.let { file ->
                        artifact(file) {
                            classifier = "javadoc"
                            extension = "jar"
                        }
                    }
                    pom { target.configurePom(this, false) }
                }
            }
        }

        // Attach all needed tasks into assembleRelease task
        target.attachAllTasksIntoAssembleRelease()

        // Setup Signing
        target.setupSigning()
    }
}
