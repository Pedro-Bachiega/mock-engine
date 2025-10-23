package com.pedrobneto.mock.engine.client.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.pedrobneto.mock.engine.client.model.FileOptions
import com.pedrobneto.mock.engine.client.model.MockOption

internal data class MockState(
    val allowCustomJson: Boolean = true,
    val chosenMockOption: MockOption? = null,
    val currentMockOptionList: List<FileOptions>? = null
)


interface MockEngineChoiceScope {
    val allowCustomJson: Boolean
    val optionList: List<FileOptions>

    fun cancel()
    fun selectOption(option: MockOption)
}

private class MockEngineChoiceScopeImpl(state: MockState) : MockEngineChoiceScope {
    override val allowCustomJson: Boolean = state.allowCustomJson
    override val optionList: List<FileOptions> = state.currentMockOptionList.orEmpty()

    override fun cancel() {
        mockState.value = MockState()
    }

    override fun selectOption(option: MockOption) {
        mockState.value = MockState(chosenMockOption = option)
    }
}

internal val mockState: MutableState<MockState> = mutableStateOf(MockState())

@Composable
fun MockEngineChoiceView(content: @Composable MockEngineChoiceScope.() -> Unit) {
    val state by mockState
    content.invoke(MockEngineChoiceScopeImpl(state))
}
