package com.mockengine.plugin.util

import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.Sign

internal fun Project.setupSigning() {
    val missingVariables = getMissingVariables(
        "signing.keyId",
        "signing.password",
        "signing.secretKeyRingFile",
    )
    if (missingVariables.isNotEmpty()) {
        println("Skipping signing. Reason:\nMissing env variables: ${missingVariables.joinToString()}")
        return
    }

    plugins.apply("signing")
    tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(project.tasks.withType(Sign::class.java))
    }
    with(sign) {
        sign(publishing.publications)
        setRequired { gradle.taskGraph.allTasks.any { (it is PublishToMavenRepository) } }
    }
}
