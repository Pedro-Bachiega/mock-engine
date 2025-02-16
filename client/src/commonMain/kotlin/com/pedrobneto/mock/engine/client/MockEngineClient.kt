package com.pedrobneto.mock.engine.client

import com.pedrobneto.mock.engine.client.model.MockData
import com.pedrobneto.mock.engine.client.resources.getFileContentFromResources
import com.pedrobneto.mock.engine.client.view.MockState
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.callContext
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.websocket.WebSocketCapability
import io.ktor.client.plugins.websocket.WebSocketExtensionsCapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

@DslMarker
@RequiresOptIn
annotation class MockEngineInternalApi

@MockEngineInternalApi
val mockConfigurationPerRequest = mutableMapOf<String, Pair<KSerializer<*>, List<String>>>()

@OptIn(MockEngineInternalApi::class)
class MockEngine internal constructor(override val config: Config) :
    HttpClientEngineBase("MockEngine") {

    private val contextState: CompletableJob = Job()

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    init {
        check(config.baseUrl.isNotEmpty()) { "No base url provided in [MockEngine.Config]" }
    }

    @OptIn(InternalAPI::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val json = config.onProvideJson.invoke()
        val callContext = callContext()

        return withContext(dispatcher + callContext) {
            val requestPath = data.url.toString().removePrefix(config.baseUrl)
            println("[MockEngine] Processing request for path: $requestPath")

            val (serializer, filePaths) = mockConfigurationPerRequest[requestPath]
                ?.takeIf { it.second.isNotEmpty() }
                ?: error("[MockEngine] No files found for path: $requestPath")

            val mockDataList: List<MockData> = filePaths.mapNotNull { path ->
                getFileContentFromResources(path)
                    ?.let { json.decodeFromString<MockData>(it) }
                    ?.takeIf { it.options.isNotEmpty() }
                    ?.also { println("[MockEngine] Found file: $it") }
            }.ifEmpty { error("[MockEngine] No files found for path: $requestPath") }

            val allOptions = mockDataList.flatMap(MockData::options)
            val isSingleOptionMock = allOptions.size == 1

            if (isSingleOptionMock) {
                MockState.chosenMockOption = allOptions.first()
            } else {
                MockState.currentMockDataList = mockDataList
                while (MockState.currentMockDataList != null && MockState.chosenMockOption == null) {
                    delay(50L)
                }
            }

            val option = MockState.chosenMockOption ?: error("[MockEngine] No option selected")
            val parsedResponse = option.responseFile?.let { path ->
                getFileContentFromResources("/$path")?.let { content ->
                    Json.decodeFromString(serializer, content)
                } ?: error("[MockEngine] Couldn't deserialize content")
            } ?: Unit
            println("[MockEngine] Response: $parsedResponse")

            HttpResponseData(
                statusCode = HttpStatusCode.fromValue(option.statusCode),
                requestTime = GMTDate(),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                version = HttpProtocolVersion.HTTP_1_1,
                body = parsedResponse,
                callContext = callContext
            )
        }
    }

    override fun close() {
        super.close()
        coroutineContext[Job]?.invokeOnCompletion { contextState.complete() }
    }

    class Config internal constructor(
        var baseUrl: String = "",
        var onProvideJson: () -> Json = { error("[MockEngine.Config] Couldn't retrieve Json instance") }
    ) : HttpClientEngineConfig()

    companion object : HttpClientEngineFactory<Config> {
        override fun create(block: Config.() -> Unit): HttpClientEngine =
            MockEngine(Config().apply(block))
    }
}

fun HttpClientConfig<*>.onMockEngine(block: MockEngine.Config.() -> Unit) = engine {
    if (this is MockEngine.Config) block.invoke(this)
}
