package com.pedrobneto.mock.engine.client

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.launchApplication
import com.pedrobneto.mock.engine.client.view.mockState
import kotlinx.coroutines.CoroutineScope

internal actual fun CoroutineScope.requestMockChoiceView() {
    launchApplication {
        val state by mockState

        LaunchedEffect(state) {
            if (state.chosenMockOption != null) exitApplication()
        }

        Window(onCloseRequest = ::exitApplication, title = "Mock Engine") {
            MockEngineClient.instance?.config?.onDrawChoiceView?.invoke()
        }
    }
}
