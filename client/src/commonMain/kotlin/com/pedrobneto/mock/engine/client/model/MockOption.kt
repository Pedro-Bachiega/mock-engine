package com.pedrobneto.mock.engine.client.model

import com.pedrobneto.mock.engine.client.resources.getFileContentFromResources
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

sealed class MockOption {

    abstract val statusCode: Int

    protected abstract fun deserialize(
        coroutineContext: CoroutineContext,
        serializer: KSerializer<*>,
        json: Json
    ): Any

    internal fun buildResponse(
        coroutineContext: CoroutineContext,
        serializer: KSerializer<*>,
        json: Json
    ) = HttpResponseData(
        statusCode = HttpStatusCode.fromValue(statusCode),
        requestTime = GMTDate(),
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
        version = HttpProtocolVersion.HTTP_1_1,
        body = deserialize(coroutineContext, serializer, json),
        callContext = coroutineContext
    )

    @Serializable
    data class Default(
        @SerialName("description") val description: String,
        @SerialName("response_file") val responseFile: String,
        @SerialName("status_code") override val statusCode: Int,
    ) : MockOption() {
        override fun deserialize(
            coroutineContext: CoroutineContext,
            serializer: KSerializer<*>,
            json: Json
        ): Any = getFileContentFromResources("/$responseFile")
            ?.let { content -> json.decodeFromString(serializer, content) }
            ?.also { println("[MockEngine] Response: $it") }
            ?: error("[MockEngine] Couldn't deserialize content from file '/$responseFile'")
    }

    @Serializable
    data class Custom(
        override val statusCode: Int,
        val responseJson: String
    ) : MockOption() {
        override fun deserialize(
            coroutineContext: CoroutineContext,
            serializer: KSerializer<*>,
            json: Json
        ): Any = json.decodeFromString(serializer, responseJson)
            ?.also { println("[MockEngine] Response: $it") }
            ?: error("[MockEngine] Couldn't deserialize content '$responseJson'")
    }
}
