package com.pedrobneto.mock.engine.processor

import com.pedrobneto.mock.engine.processor.model.FunctionData

internal fun mockDataTemplate(mocks: List<FunctionData>): String {
    val mockedDataEntries = mocks.joinToString(separator = ",\n") { functionData ->
        val (typeName, typeParameters) = functionData.returnType
        val serializerName = if (typeParameters.isEmpty()) {
            "serializer<$typeName>()"
        } else {
            "serializer<$typeName<${typeParameters.joinToString(", ")}>>()"
        }

        """
            Pair(
                "${functionData.requestPath}",
                Pair(
                    $serializerName,
                    listOf(
                        "${functionData.filePaths.joinToString(",\n") { "/$it" }}"
                    )
                )
            )"""
    }

    return """
package $MOCK_ENGINE_PACKAGE

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

object $MOCK_ENGINE_DATA_FILE_NAME {
    @OptIn(MockEngineInternalApi::class)
    fun loadMocks() {
        mockConfigurationPerRequest += mapOf<String, Pair<KSerializer<*>, List<String>>>(
            $mockedDataEntries
        )
    }
}
    """.trimIndent()
}