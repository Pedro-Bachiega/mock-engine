package com.mockengine.plugin.util

import com.android.build.gradle.internal.crash.afterEvaluate
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

@Suppress("UnusedReceiverParameter")
internal fun Project.attachAllTasksIntoAssembleRelease() = afterEvaluate { project ->
    val all = project.tasks.filter { task ->
        if (task is Jar || task is Javadoc) task.name.contains("debug", true)
        else false
    }.map { project.tasks.named(it.name) }

    project.tasks.findByName("assembleRelease")?.dependsOn(all)
}
