@file:Suppress("UnstableApiUsage")

pluginManagement {
    apply(from = "$rootDir/kmp-build-plugin/repositories.gradle.kts")
    val repositoryList: RepositoryHandler.() -> Unit by extra
    repositories(repositoryList)
}

dependencyResolutionManagement {
    apply(from = "$rootDir/kmp-build-plugin/repositories.gradle.kts")
    val repositoryList: RepositoryHandler.() -> Unit by extra
    repositories(repositoryList)

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "MockEngine"

includeBuild("kmp-build-plugin")

include(
//    ":sample",

    ":annotation",
    ":client",
    ":processor",

    ":gradle-plugin",
)
