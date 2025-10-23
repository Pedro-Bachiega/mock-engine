package com.pedrobneto.mock.engine.client

import com.pedrobneto.mock.engine.client.model.FileOptions
import com.pedrobneto.mock.engine.client.model.MockConfiguration
import com.pedrobneto.mock.engine.client.model.MockEngineApi
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

private val mockConfigurationPerRequest = mutableSetOf<MockConfiguration>()

fun HttpClientConfig<*>.onMockEngine(block: MockEngine.Config.() -> Unit) = engine {
    if (this is MockEngine.Config) block.invoke(this)
}

@MockEngineApi
fun addMockConfiguration(config: MockConfiguration) {
    if (config.filePaths.isEmpty() && !config.allowCustomJson) {
        println(
            "[MockEngine] Skipping configuration for ${config.requestPath}. " +
                    "Reason: No file paths provided and custom json not allowed."
        )
        return
    }

    mockConfigurationPerRequest += config
}

@MockEngineApi
fun addMockConfigurations(vararg configs: MockConfiguration) =
    configs.forEach(::addMockConfiguration)

class MockEngine internal constructor(override val config: Config) :
    HttpClientEngineBase("MockEngine") {

    private val contextState: CompletableJob = Job()

    override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> = setOf(
        HttpTimeoutCapability,
        WebSocketCapability,
        WebSocketExtensionsCapability
    )

    init {
        check(config.baseUrl.isNotEmpty()) {
            "[MockEngine] No base url provided in [MockEngine.Config]"
        }
    }

    @OptIn(InternalAPI::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val json = config.onProvideJson.invoke()
        val callContext = callContext()

        return withContext(dispatcher + callContext) {
            val requestPath = data.url.toString().removePrefix(config.baseUrl)
            val mockConfiguration = getRequestFiles(requestPath)

            val mockOptionsList = mockConfiguration.getMockOptionList(json)
            val serializer = mockConfiguration.serializer

            val allOptions = mockOptionsList.flatMap(FileOptions::options)
            if (allOptions.size == 1) {
                MockState.chosenMockOption = allOptions.first()
            } else {
                MockState.allowCustomJson = mockConfiguration.allowCustomJson
                MockState.currentMockOptionList = mockOptionsList
                while (MockState.currentMockOptionList != null && MockState.chosenMockOption == null) {
                    delay(50L)
                }
            }

            val response = MockState.chosenMockOption
                ?.buildResponse(callContext, serializer, json)
                ?: error("[MockEngine] Option could not be deserialized")

            response.also {
                MockState.allowCustomJson = false
                MockState.chosenMockOption = null
                MockState.currentMockOptionList = null
            }
        }
    }

    override fun close() {
        super.close()
        coroutineContext[Job]?.invokeOnCompletion { contextState.complete() }
    }

    private fun getRequestFiles(requestPath: String): MockConfiguration =
        mockConfigurationPerRequest.firstNotNullOfOrNull { configuration ->
            configuration.takeIf {
                (it.allowCustomJson || it.filePaths.isNotEmpty()) && requestPath.matches(
                    it.requestPath.replace("/\\{.+\\}/".toRegex(), "/[^/]+/")
                        .replace("(https|http)://(.+)((:\\d+)*)/(.*)".toRegex(), "$1://$2/$5")
                        .toRegex()
                )
            }
        } ?: error("[MockEngine] No mocks found for path: $requestPath")

    class Config internal constructor(
        var baseUrl: String = "",
        var onProvideJson: () -> Json = {
            error("[MockEngine.Config] Couldn't retrieve Json instance")
        }
    ) : HttpClientEngineConfig()

    companion object : HttpClientEngineFactory<Config> {
        override fun create(block: Config.() -> Unit): HttpClientEngine =
            MockEngine(Config().apply(block))
    }
}
