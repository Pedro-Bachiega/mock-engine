package com.pedrobneto.mock.engine.client.model

import com.pedrobneto.mock.engine.client.resources.getFileContentFromResources
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

data class MockConfiguration(
    val requestPath: String,
    val allowCustomJson: Boolean,
    val serializer: KSerializer<*>,
    val filePaths: List<String>
) {
    internal fun getMockOptionList(json: Json): List<FileOptions> = filePaths.mapNotNull { path ->
        getFileContentFromResources(path)?.let {
            val options = json.decodeFromString<List<MockOption.Default>>(it).ifEmpty {
                println("[MockEngine] File '$path' has no options")
                return@let null
            }

            FileOptions(fileName = path, options = options)
        }
    }
}
