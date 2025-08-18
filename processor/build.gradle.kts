import com.vanniktech.maven.publish.KotlinJvm
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    signing
    alias(libs.plugins.vanniktech.publish)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

publishing {
//    publications {
//        create<MavenPublication>("default") {
//            from(components["java"])
//
//            groupId = "io.github.pedro-bachiega"
//            artifactId = "mock-engine-processor"
//            version = "0.0.1"
//
//            pom {
//                name.set(project.name)
//                issueManagement {
//                    system.set("GitHub")
//                    url.set("https://github.com/Foso/Ktorfit/issues")
//                }
//                description.set("KSP Plugin for Ktorfit")
//                url.set("https://github.com/Foso/Ktorfit")
//
//                licenses {
//                    license {
//                        name.set("Apache License 2.0")
//                        url.set("https://github.com/Foso/Ktorfit/blob/master/LICENSE.txt")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/Foso/Ktorfit")
//                    connection.set("scm:git:git://github.com/Foso/Ktorfit.git")
//                }
//                developers {
//                    developer {
//                        name.set("Jens Klingenberg")
//                        url.set("https://github.com/Foso")
//                    }
//                }
//            }
//        }
//    }

    repositories { mavenLocal() }
}

mavenPublishing {
    configure(KotlinJvm())
//    coordinates(
//        "io.github.pedro-bachiega",
//        "mock-engine-processor",
//        "0.0.1",
//    )
//    @Suppress("UnstableApiUsage")
//    pomFromGradleProperties()
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}

dependencies {
    implementation(project(":annotation"))

    // Google
    implementation(libs.google.ksp)
}

// region Version
version = runCatching {
    providers.gradleProperty("VERSION_NAME").get().takeIf { it.isNotEmpty() }
}.getOrNull() ?: runGitCommand(
    fileName = "version-name.txt",
    command = "git describe",
    default = "0.0.1",
)

private val String.execute: Process
    get() = Runtime.getRuntime().exec(this.split(" ").toTypedArray())

private val Process.text: String
    get() = inputStream.bufferedReader().readText().trim()

private val String.executeWithText: String?
    get() {
        val process = execute
        if (process.waitFor() != 0) return null
        return process.text
    }

private fun Project.runGitCommand(fileName: String, command: String, default: String): String {
    val file = File("$rootDir/build", fileName)
    if (file.exists()) return file.readText().trim()
    if (validateGit().not()) return default

    val output = command.executeWithText
    return if (output.isNullOrBlank()) {
        default
    } else {
        file.parentFile.mkdirs()
        output.also(file::writeText)
    }
}

private fun validateGit(): Boolean {
    val command = if (Os.isFamily(Os.FAMILY_WINDOWS)) "git --version" else "whereis git"
    val output = command.executeWithText
    return (output.isNullOrEmpty() || output == "git:").not()
}
// endregion
