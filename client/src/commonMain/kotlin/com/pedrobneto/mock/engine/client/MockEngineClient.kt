package com.pedrobneto.mock.engine.client

import com.pedrobneto.mock.engine.client.model.MockData
import com.pedrobneto.mock.engine.client.model.MockEngineApi
import com.pedrobneto.mock.engine.client.model.MockEngineInternalApi
import com.pedrobneto.mock.engine.client.model.RequestData
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
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@MockEngineInternalApi
private val mockConfigurationPerRequest = mutableMapOf<String, RequestData>()

@OptIn(MockEngineInternalApi::class)
@MockEngineApi
fun addMockConfiguration(path: String, requestData: RequestData) {
    if (requestData.filePaths.isEmpty()) {
        println("[MockEngine] Skipping request data for $path. Reason: No file paths provided")
        return
    }

    mockConfigurationPerRequest[path] = requestData
}

@MockEngineApi
fun addMockConfigurations(map: Map<String, RequestData>) =
    map.forEach { (path, requestData) -> addMockConfiguration(path, requestData) }

class MockEngine internal constructor(override val config: Config) :
    HttpClientEngineBase("MockEngine") {

    private val contextState: CompletableJob = Job()

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    init {
        check(config.baseUrl.isNotEmpty()) { "[MockEngine] No base url provided in [MockEngine.Config]" }
    }

    @OptIn(InternalAPI::class, MockEngineInternalApi::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val json = config.onProvideJson.invoke()
        val callContext = callContext()

        return withContext(dispatcher + callContext) {
            val requestPath = data.url.toString().removePrefix(config.baseUrl)
            println("[MockEngine] Processing request for path: $requestPath")

            val requestData = mockConfigurationPerRequest[requestPath]
                ?: error("[MockEngine] No request data for path: $requestPath")
            val mockDataList = requestData.getMockDataList(json)
                .ifEmpty { error("[MockEngine] No files found for path: $requestPath") }

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

            val response = MockState.chosenMockOption
                ?.deserialize(callContext, requestData.serializer, json)

            MockState.currentMockDataList = null
            MockState.chosenMockOption = null

            response ?: error("[MockEngine] No option selected")
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
