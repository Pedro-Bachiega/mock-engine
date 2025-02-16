package com.mockengine.plugin.gradle

import com.mockengine.plugin.util.configurePom
import com.mockengine.plugin.util.createLocalPathRepository
import com.mockengine.plugin.util.createSonatypeRepository
import com.mockengine.plugin.util.publishing
import com.mockengine.plugin.util.setupSigning
import com.mockengine.plugin.util.versionName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

internal class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // Setup Publishing
        with(target.publishing) {
            repositories {
                createLocalPathRepository(target)
                createSonatypeRepository(target)
            }

            publications {
                create<MavenPublication>(target.name) {
                    from(target.components["java"])

                    groupId = target.property("GROUP_ID") as String
                    artifactId = "mock-engine-${target.name}"
                    version = target.versionName

                    pom { target.configurePom(this, false) }
                }
            }
        }

        // Setup Signing
        target.setupSigning()
    }
}
