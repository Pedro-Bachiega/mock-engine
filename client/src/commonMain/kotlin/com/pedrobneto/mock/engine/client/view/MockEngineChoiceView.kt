package com.pedrobneto.mock.engine.client.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.pedrobneto.mock.engine.client.model.FileOptions
import com.pedrobneto.mock.engine.client.model.MockOption
import kotlinx.coroutines.delay

internal object MockState {
    var allowCustomJson: Boolean = false
    var chosenMockOption: MockOption? = null
    var currentMockOptionList: List<FileOptions>? = null

    fun chooseOption(option: MockOption) {
        allowCustomJson = false
        chosenMockOption = option
        currentMockOptionList = null
    }
}

@Composable
fun MockEngineChoiceView(
    content: @Composable (
        allowCustomJson: Boolean,
        optionList: List<FileOptions>,
        onOptionSelected: (option: MockOption) -> Unit
    ) -> Unit
) {
    var shouldShow by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect("MockEngine") {
        while (true) {
            if (MockState.currentMockOptionList != null && !shouldShow) shouldShow = true
            delay(250L)
        }
    }

    if (shouldShow) {
        val optionList = MockState.currentMockOptionList ?: return
        content.invoke(MockState.allowCustomJson, optionList) {
            MockState.chooseOption(it)
            shouldShow = false
        }
    }
}
