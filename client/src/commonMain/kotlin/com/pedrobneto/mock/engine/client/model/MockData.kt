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

@Serializable
data class MockData(val options: List<Option>) {
    @Serializable
    data class Option(
        val description: String,
        @SerialName("status_code") val statusCode: Int,
        @SerialName("response_file") val responseFile: String? = null,
    ) {
        internal fun deserialize(
            coroutineContext: CoroutineContext,
            serializer: KSerializer<*>,
            json: Json
        ): HttpResponseData {
            val parsedResponse = getFileContentFromResources("/$responseFile")
                ?.let { content -> json.decodeFromString(serializer, content) }
                ?.also { println("[MockEngine] Response: $it") }
                ?: error("[MockEngine] Couldn't deserialize content")

            return HttpResponseData(
                statusCode = HttpStatusCode.fromValue(statusCode),
                requestTime = GMTDate(),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                version = HttpProtocolVersion.HTTP_1_1,
                body = parsedResponse,
                callContext = coroutineContext
            )
        }
    }
}
