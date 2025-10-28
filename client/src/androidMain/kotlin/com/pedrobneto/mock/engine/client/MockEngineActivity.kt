package com.pedrobneto.mock.engine.client

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

internal class MockEngineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MockEngineClient.instance?.config?.onDrawChoiceView?.invoke() }
    }
}
