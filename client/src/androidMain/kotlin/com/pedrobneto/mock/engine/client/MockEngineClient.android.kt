package com.pedrobneto.mock.engine.client

import android.content.Intent
import br.com.arch.toolkit.util.ContextProvider
import kotlinx.coroutines.CoroutineScope

internal actual fun CoroutineScope.requestMockChoiceView() {
    val context = ContextProvider.current
    if (context == null || MockEngineClient.instance == null) return

    context.startActivity(Intent(context, MockEngineActivity::class.java))
}
