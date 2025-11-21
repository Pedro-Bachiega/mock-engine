package com.pedrobneto.mock.engine.processor

import br.com.arch.toolkit.lumber.DebugTree
import br.com.arch.toolkit.lumber.Lumber
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.pedrobneto.mock.engine.annotation.Mock
import com.pedrobneto.mock.engine.processor.model.FunctionData

private const val MOCK_ENGINE_PACKAGE = "com.pedrobneto.mock.engine.client"
private const val MOCK_ENGINE_DATA_FILE_NAME = "MockEngine"

class MockEngineProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MockEngineProcessor(environment)
}

internal class MockEngineProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {

    private var invoked = false
    private var mocksFound = false

    init {
        Lumber.plant(DebugTree())
    }

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> = runCatching {
        if (invoked && mocksFound) return emptyList()

        invoked = true

        val annotatedFunctions = resolver
            .getSymbolsWithAnnotation(annotationName = Mock::class.qualifiedName!!, inDepth = true)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()
        val functionDataList = annotatedFunctions
            .mapNotNull { FunctionData(it, environment.logger) }

        mocksFound = functionDataList.isNotEmpty()
        if (!mocksFound) {
            Lumber.debug("[MockEngineProcessor] No mocks found")
            resolver.getKotlinClassByName("$MOCK_ENGINE_PACKAGE.$MOCK_ENGINE_DATA_FILE_NAME")
                ?.let {
                    Lumber.debug("[MockEngineProcessor] Generated class found, not overwriting")
                    return emptyList()
                }
        }

        environment.codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = MOCK_ENGINE_PACKAGE,
            fileName = MOCK_ENGINE_DATA_FILE_NAME.removePrefix("")
        ).use { it.write(mockDataTemplate(mocks = functionDataList).toByteArray()) }

        return emptyList()
    }.getOrDefault(emptyList())

    private fun mockDataTemplate(mocks: List<FunctionData>): String {
        val mockedDataEntries = mocks.joinToString(separator = "\n") { functionData ->
            val (typeName, typeParameters) = functionData.returnType
            val serializer = if (typeParameters.isEmpty()) {
                "serializer<$typeName>()"
            } else {
                "serializer<$typeName<${typeParameters.joinToString(", ")}>>()"
            }

            """MockConfiguration(
                    requestPath = "${functionData.requestPath}",
                    serializer = $serializer,
                    filePaths = listOf(
                        "${functionData.filePaths.joinToString(",\n") { "/$it" }}"
                    )
                ),""".trimIndent()
        }

        return """
package $MOCK_ENGINE_PACKAGE

import com.pedrobneto.mock.engine.client.model.MockConfiguration
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.serializer

object $MOCK_ENGINE_DATA_FILE_NAME : HttpClientEngineFactory<MockEngineClient.Config> {
    init {
    ${
            if (mockedDataEntries.isNotEmpty()) {
                """
            addMockConfigurations(
            $mockedDataEntries
        )
        """.trimIndent()
            } else {
                ""
            }
        }
    }

    override fun create(block: MockEngineClient.Config.() -> Unit): HttpClientEngine =
        MockEngineClient(block)
}""".trimIndent()
    }
}
