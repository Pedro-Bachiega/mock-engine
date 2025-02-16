package com.pedrobneto.mock.engine.client.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrobneto.mock.engine.client.model.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal object MockState {
    var currentMockDataList: List<MockData>? = null
    var chosenMockOption: MockData.Option? = null

    fun chooseOption(option: MockData.Option) {
        currentMockDataList = null
        chosenMockOption = option
    }
}

@Composable
fun MockEngineChoiceView(
    content: @Composable (dataList: List<MockData>, onOptionSelected: (MockData.Option) -> Unit) -> Unit
) {
    var shouldShow by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect("MockEngine") {
        while (true) {
            if (MockState.currentMockDataList != null && !shouldShow) shouldShow = true
            delay(250L)
        }
    }

    if (shouldShow) {
        val dataList = MockState.currentMockDataList ?: return
        content.invoke(dataList) {
            MockState.chooseOption(it)
            shouldShow = false
        }
    }
}

@Composable
fun DefaultMockEngineChoiceView() {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    MockEngineChoiceView { dataList, onOptionSelected ->
        val options = dataList.flatMap(MockData::options)

        ModalBottomSheet(
            modifier = Modifier.padding(bottom = 16.dp),
            sheetState = sheetState,
            onDismissRequest = { onOptionSelected.invoke(options.first()) },
        ) {
            options.forEach { option ->
                Column(
                    modifier = Modifier.clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onOptionSelected.invoke(option)
                        }
                    }.fillMaxWidth().padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    Text(text = "[${option.statusCode}] ${option.description}", fontSize = 14.sp)
                    option.responseFile?.let {
                        Text(text = "Response file: $it", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
