package com.pedrobneto.mock.engine.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.launchApplication
import com.pedrobneto.mock.engine.client.model.FileOptions
import com.pedrobneto.mock.engine.client.model.MockConfiguration
import com.pedrobneto.mock.engine.client.model.MockEngineApi
import com.pedrobneto.mock.engine.client.view.DefaultMockEngineChoiceView
import com.pedrobneto.mock.engine.client.view.MockState
import com.pedrobneto.mock.engine.client.view.mockState
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.websocket.WebSocketCapability
import io.ktor.client.plugins.websocket.WebSocketExtensionsCapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val mockConfigurationPerRequest = mutableSetOf<MockConfiguration>()

fun HttpClientConfig<*>.onMockEngine(block: MockEngineClient.Config.() -> Unit) = engine {
    if (this is MockEngineClient.Config) block.invoke(this)
}

@MockEngineApi
fun addMockConfiguration(config: MockConfiguration) {
    mockConfigurationPerRequest += config
}

@MockEngineApi
fun addMockConfigurations(vararg configs: MockConfiguration) =
    configs.forEach(::addMockConfiguration)

class MockEngineClient(override val config: Config) :
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
                mockState.value = MockState(chosenMockOption = allOptions.first())
            } else {
                mockState.value = MockState(
                    allowCustomJson = config.allowCustomJson,
                    currentMockOptionList = mockOptionsList
                )

                runBlocking { drawMockApplication() }
            }

            mockState.value.chosenMockOption
                ?.buildResponse(callContext, serializer, json)
                ?.also { mockState.value = MockState() }
                ?: error("[MockEngine] Option could not be deserialized")
        }
    }

    override fun close() {
        super.close()
        coroutineContext[Job]?.invokeOnCompletion { contextState.complete() }
    }

    private fun getRequestFiles(requestPath: String): MockConfiguration =
        mockConfigurationPerRequest.firstNotNullOfOrNull { configuration ->
            configuration.takeIf {
                config.allowCustomJson || (it.filePaths.isNotEmpty() && requestPath.matches(
                    it.requestPath
                        .replace("\\{[^}]+\\}".toRegex(), "[^/]+")
                        .replace("(https|http)://(.+)((:\\d+)*)/(.*)".toRegex(), "$1://$2/$5")
                        .toRegex()
                ))
            }
        } ?: error("[MockEngine] No mocks found for path: $requestPath.")

    private fun CoroutineScope.drawMockApplication() = launchApplication {
        val state by mockState

        LaunchedEffect(state) {
            if (state.chosenMockOption != null) exitApplication()
        }

        Window(onCloseRequest = ::exitApplication, title = "Mock Engine") {
            config.onDrawChoiceView.invoke()
        }
    }

    class Config(
        var baseUrl: String = "",
        var allowCustomJson: Boolean = true,
        var onProvideJson: () -> Json = {
            error("[MockEngine.Config] Couldn't retrieve Json instance")
        },
        var onDrawChoiceView: @Composable () -> Unit = {
            MaterialTheme {
                DefaultMockEngineChoiceView()
            }
        }
    ) : HttpClientEngineConfig()
}
