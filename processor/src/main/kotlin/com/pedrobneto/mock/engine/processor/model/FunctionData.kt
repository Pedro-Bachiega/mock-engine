package com.pedrobneto.mock.engine.processor.model

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.pedrobneto.mock.engine.annotation.Mock

internal class FunctionData(
    val requestPath: String,
    val allowCustomJson: Boolean,
    val filePaths: List<String>,
    val returnType: Pair<String, List<String>>,
) {
    @OptIn(KspExperimental::class)
    companion object {
        private fun getRequestPath(declaration: KSFunctionDeclaration): String? =
            declaration.annotations.toList()
                .firstOrNull { HttpMethod.contains(it.shortName.asString()) }
                ?.arguments
                ?.firstOrNull { it.name?.asString() == "value" }
                ?.value as? String

        operator fun invoke(declaration: KSFunctionDeclaration, logger: KSPLogger): FunctionData? {
            val functionName = declaration.simpleName.asString()
            val requestPath = getRequestPath(declaration)
            val mockAnnotation = declaration.getAnnotationsByType(Mock::class).firstOrNull()
            if (mockAnnotation == null || requestPath == null) return null

            val files = mockAnnotation.files.map(String::trim)
            if (files.isEmpty()) {
                logger.error("No files found in mock annotation in $functionName")
                return null
            }

            val typeInfo = declaration.returnType?.resolve()?.declaration?.run {
                qualifiedName!!.asString() to
                        declaration.returnType?.element?.typeArguments?.map {
                            it.type?.resolve()?.declaration?.qualifiedName?.asString().orEmpty()
                        }.orEmpty()
            }
            if (typeInfo == null) {
                logger.error("No type found in mock annotation in $functionName")
                return null
            }

            return FunctionData(
                requestPath = requestPath,
                allowCustomJson = mockAnnotation.allowCustomJson,
                filePaths = files.toList(),
                returnType = typeInfo
            )
        }
    }
}
