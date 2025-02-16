package com.pedrobneto.mock.engine.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.pedrobneto.mock.engine.annotation.Mock
import com.pedrobneto.mock.engine.processor.model.FunctionData

internal const val MOCK_ENGINE_PACKAGE = "com.pedrobneto.mock.engine.client"
internal const val MOCK_ENGINE_DATA_FILE_NAME = "MockEngineData"

class MockEngineProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MockEngineProcessor(environment)
}

internal class MockEngineProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        invoked = true

        val functionList = resolver.getSymbolsWithAnnotation(Mock::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { FunctionData(it, environment.logger) }
            .toList()

        if (functionList.isEmpty()) {
            environment.logger.warn("[Processor] No mocks found")
            return emptyList()
        }

        environment.codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = MOCK_ENGINE_PACKAGE,
            fileName = MOCK_ENGINE_DATA_FILE_NAME.removePrefix("")
        ).use { it.write(mockDataTemplate(mocks = functionList).toByteArray()) }

        return emptyList()
    }
}
