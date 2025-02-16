@file:Suppress("UnstableApiUsage")

pluginManagement {
    apply(from = "$rootDir/repositories.gradle.kts")
    val repositoryList: RepositoryHandler.() -> Unit by extra
    repositories(repositoryList)
}

dependencyResolutionManagement {
    apply(from = "$rootDir/repositories.gradle.kts")
    val repositoryList: RepositoryHandler.() -> Unit by extra
    repositories(repositoryList)

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "MockEngine"

includeBuild("build-plugin")

include(
//    ":sample",

    ":annotation",
    ":client",
    ":processor",

    ":gradle-plugin",
)
