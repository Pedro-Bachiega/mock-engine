package com.pedrobneto.mockengine.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.arch.toolkit.compose.collectAsComposableState
import br.com.arch.toolkit.result.EventDataStatus
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun App(viewModel: SampleViewModel = koinViewModel()) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var customPath by remember { mutableStateOf("") }

        val sampleModel by viewModel.sampleFlow.collectAsComposableState()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            sampleModel.Unwrap {
                OnShowLoading {
                    Text(text = "Loading...")
                }
                OnError { throwable ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error")
                        Text(text = throwable.message ?: "Unknown error")
                    }
                }
                OnSuccess(dataStatus = EventDataStatus.WithoutData) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Empty")
                        Text(text = "Click \"Fetch\" to fetch your mocked data")
                    }
                }
                OnData { (title, description) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = title)
                        Text(text = description)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { scope.launch { viewModel.fetchSampleModel(customPath) } },
                content = { Text(text = "Fetch") }
            )
        }
    }
}
