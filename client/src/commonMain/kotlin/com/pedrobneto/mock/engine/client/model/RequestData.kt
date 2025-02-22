package com.pedrobneto.mock.engine.client.model

import com.pedrobneto.mock.engine.client.resources.getFileContentFromResources
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

data class RequestData(
    internal val serializer: KSerializer<*>,
    internal val filePaths: List<String>,
) {
    fun getMockDataList(json: Json): List<MockData> = filePaths.mapNotNull { path ->
        getFileContentFromResources(path)
            ?.let { json.decodeFromString<MockData>(it) }
            ?.takeIf { it.options.isNotEmpty() }
            ?.also { println("Found file: $it") }
    }
}
