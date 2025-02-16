plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.dsl)

    alias(libs.plugins.lint.detekt)
    alias(libs.plugins.lint.ktlint)
    id(libs.plugins.quality.jacoco.get().pluginId)
}

kotlin { jvmToolchain(17) }
jacoco { toolVersion = libs.versions.jacoco.get() }

group = "com.mockengine.plugin"
version = "1.0.0"

dependencies {
    compileOnly(gradleApi())

    implementation(libs.plugin.androidx.plugin)
    implementation(libs.plugin.lint.detekt)
    implementation(libs.plugin.lint.ktlint)
    implementation(libs.plugin.jetbrains.dokka)
    implementation(libs.plugin.jetbrains.extensions)
    implementation(libs.plugin.jetbrains.kotlin.plugin)
    implementation(libs.plugin.jetbrains.kover)
    implementation(libs.plugin.gradle.publish)
}

sourceSets {
    main {
        java { srcDirs("src/main/java") }
        kotlin { srcDirs("src/main/kotlin") }
    }
}

gradlePlugin {
    plugins {
        //region Android
        create("plugin-android-application") {
            id = "plugin-android-application"
            displayName = "Android Application Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.android.ApplicationPlugin"
        }
        //endregion

        //region Browser
        create("plugin-browser-application") {
            id = "plugin-browser-application"
            displayName = "Browser Application Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.browser.ApplicationPlugin"
        }
        //endregion

        //region Desktop
        create("plugin-desktop-application") {
            id = "plugin-desktop-application"
            displayName = "Desktop Application Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.desktop.ApplicationPlugin"
        }
        //endregion

        //region Multiplatform
        create("plugin-multiplatform-library") {
            id = "plugin-multiplatform-library"
            displayName = "Multiplatform Library Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.multiplatform.LibraryPlugin"
        }
        create("plugin-multiplatform-publish") {
            id = "plugin-multiplatform-publish"
            displayName = "Multiplatform Publish Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.multiplatform.PublishPlugin"
        }
        //endregion

        //region Gradle
        create("plugin-gradle-publish") {
            id = "plugin-gradle-publish"
            displayName = "Gradle Publish Plugin"
            description = "\\o/"
            implementationClass = "com.mockengine.plugin.gradle.PublishPlugin"
        }
        //endregion

        //region Generic
        create("plugin-compose") {
            id = "plugin-compose"
            displayName = "Compose Plugin"
            description = "Compose Plugin"
            implementationClass = "com.mockengine.plugin.ComposePlugin"
        }
        create("plugin-optimize") {
            id = "plugin-optimize"
            displayName = "Optimization Plugin"
            description = "Optimize dependencies"
            implementationClass =
                "com.mockengine.plugin.OptimizeDependenciesAndFilterTasksPlugin"
        }
        create("plugin-lint") {
            id = "plugin-lint"
            displayName = "Lint Plugin"
            description = "Enables and configure lint for module"
            implementationClass = "com.mockengine.plugin.LintPlugin"
        }

        create("plugin-test") {
            id = "plugin-test"
            displayName = "Test Plugin"
            description = "Enables and configure test for module"
            implementationClass = "com.mockengine.plugin.TestPlugin"
        }
        //endregion
    }
}
