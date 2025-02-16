package com.mockengine.plugin.util

import groovy.util.Node
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication

internal fun RepositoryHandler.createLocalPathRepository(project: Project) = maven {
    name = "LocalPath"
    url = project.uri(project.rootProject.layout.buildDirectory.asFile.get().absolutePath)
}

internal fun RepositoryHandler.createSonatypeRepository(project: Project) {
    val missingVariables = project.getMissingVariables(
        "OSSRH_USERNAME",
        "OSSRH_PASSWORD"
    )
    if (missingVariables.isNotEmpty()) {
        error("Skipping sonatype repository. Reason:\nMissing env variables: ${missingVariables.joinToString()}")
//        println("Skipping sonatype repository. Reason:\nMissing env variables: ${missingVariables.joinToString()}")
//        return
    }

    maven {
        name = "Sonatype"
        url = project.uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials {
            username =
                System.getenv("OSSRH_USERNAME") ?: (project.properties["OSSRH_USERNAME"] as String)
            password =
                System.getenv("OSSRH_PASSWORD") ?: (project.properties["OSSRH_PASSWORD"] as String)
        }
    }
}

internal fun Project.configurePom(pom: MavenPom, addDependencies: Boolean = true) {
    // Main Configuration
    if (pom.name.orNull.isNullOrBlank() && hasProperty("NAME")) {
        pom.name.set(properties["NAME"] as String)
    }
    if (pom.description.orNull.isNullOrBlank() && hasProperty("DESCRIPTION")) {
        pom.description.set(properties["DESCRIPTION"] as String)
    }
    if (pom.url.orNull.isNullOrBlank() && hasProperty("REPO_URL")) {
        pom.url.set(properties["REPO_URL"] as String)
    }

    // SCM
    pom.scm {
        url.set(properties["REPO_GIT_URL"] as String)
    }

    // Developer Configuration
    pom.developers {
        developer {
            id.set(this@configurePom.properties["DEVELOPER_ID"] as String)
            name.set(this@configurePom.properties["DEVELOPER_NAME"] as String)
            email.set(this@configurePom.properties["DEVELOPER_EMAIL"] as String)
            organization.set("None :)")
            url.set(this@configurePom.properties["DEVELOPER_URL"] as String)
        }
    }

    // License Configuration
    pom.licenses {
        license {
            name.set(properties["LICENSE_NAME"] as String)
            url.set(properties["LICENSE_URL"] as String)
            distribution.set(properties["LICENSE_DIST"] as String)
        }
    }

    pom.ciManagement {
        system.set("GitHub Actions")
        url.set("${pom.url.orNull}/actions")
    }

    if (addDependencies.not()) return
    val mapOfConfigurations = mapOf(
        "runtime" to "implementation",
        "compile" to "api",
        "provided" to "compileOnly"
    ).mapNotNull { (scope, configuration) ->
        configurations.findByName(configuration)?.let { scope to it }
    }.toMap()
    if (mapOfConfigurations.isNotEmpty()) {
        pom.withXml {
            val dependencyNode: Node = asNode().appendNode("dependencies")
            mapOfConfigurations.forEach { (scope, configuration) ->
                configuration.dependencies.forEach { dependencyNode.addDependency(it, scope) }
            }
        }
    }
}

private fun Node.addDependency(dependency: Dependency, scope: String) {
    val projectDependency =
        DefaultGroovyMethods.getProperties(dependency)["dependencyProject"] as? Project

    if (projectDependency == null) {
        val group = dependency.group.takeIf { it.isNullOrBlank().not() }
        val name = dependency.name.takeIf { it.isBlank().not() }
        val version = dependency.version.takeIf { it.isNullOrBlank().not() }

        if (group == null || name == null || version == null) return

        val node = appendNode("dependency")
        node.appendNode("groupId", group)
        node.appendNode("artifactId", name)
        node.appendNode("version", version)
        node.appendNode("scope", scope)
    } else {
        val publishExtension = projectDependency.publishing
        publishExtension.publications.filterIsInstance<MavenPublication>().onEach { pub ->
            val node = appendNode("dependency")
            node.appendNode("groupId", pub.groupId)
            node.appendNode("artifactId", pub.artifactId)
            node.appendNode("version", pub.version)
            node.appendNode("scope", scope)
        }
    }
}
