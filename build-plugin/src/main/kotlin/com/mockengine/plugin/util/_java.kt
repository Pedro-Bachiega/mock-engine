package com.mockengine.plugin.util

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal val projectJavaVersion: JavaVersion = JavaVersion.VERSION_17
internal val projectJavaTarget: JvmTarget = JvmTarget.JVM_17
internal const val projectJavaVersionCode: Int = 17
internal const val projectJavaVersionName: String = projectJavaVersionCode.toString()
