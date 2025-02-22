package com.pedrobneto.mock.engine.processor

import com.pedrobneto.mock.engine.processor.model.FunctionData

internal fun mockDataTemplate(mocks: List<FunctionData>): String {
    val mockedDataEntries = mocks.joinToString(separator = "\n") { functionData ->
        val (typeName, typeParameters) = functionData.returnType
        val serializer = if (typeParameters.isEmpty()) {
            "serializer<$typeName>()"
        } else {
            "serializer<$typeName<${typeParameters.joinToString(", ")}>>()"
        }

        """"${functionData.requestPath}" to RequestData(
                    $serializer,
                    listOf(
                        "${functionData.filePaths.joinToString(",\n") { "/$it" }}"
                    )
                ),
        """
    }

    return """
package $MOCK_ENGINE_PACKAGE

import com.pedrobneto.mock.engine.client.model.RequestData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

object $MOCK_ENGINE_DATA_FILE_NAME {
    fun loadMocks() {
        addMockConfigurations(
            mapOf(
                $mockedDataEntries
            )
        )
    }
}
    """.trimIndent()
}