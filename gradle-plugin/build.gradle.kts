plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.jetbrains.kotlin.dsl)
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
}

apply(from = "$rootDir/versioning.gradle.kts")

kotlin { jvmToolchain(21) }

dependencies {
    compileOnly(gradleApi())
    implementation(libs.toolkit.arch.lumber)
    implementation(libs.plugin.jetbrains.kotlin.plugin)
}

sourceSets {
    main {
        java { srcDirs("src/main/java") }
        kotlin { srcDirs("src/main/kotlin") }
    }
}

gradlePlugin {
    website = providers.gradleProperty("POM_URL")
    vcsUrl = providers.gradleProperty("POM_SCM_URL")

    plugins {
        create("plugin") {
            id = "io.github.pedro-bachiega.mock-engine-compiler"
            displayName = "Mock Engine Compiler Gradle Plugin"
            description = "An easy way to mock your API calls using ktorfit and KSP"
            implementationClass = "com.pedrobneto.mock.engine.plugin.GradlePlugin"
            tags = listOf(
                "mock-engine",
                "mock-engine-compiler",
                "mock",
                "kmp",
                "multiplatform",
                "compose",
                "ktor",
                "ktorfit",
            )
        }
    }
}
