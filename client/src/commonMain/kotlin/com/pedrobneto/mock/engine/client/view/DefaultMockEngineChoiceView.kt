package com.pedrobneto.mock.engine.client.view

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrobneto.mock.engine.client.model.MockOption

@Composable
private fun MockEngineChoiceScope.OptionListView(
    modifier: Modifier = Modifier,
    onToggleState: () -> Unit
) = Column(modifier = modifier) {
    Column(
        modifier = Modifier.weight(weight = 1f)
            .verticalScroll(rememberScrollState())
    ) {
        optionList.forEach { fileOption ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mock file: ${fileOption.fileName}",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                fileOption.options.forEach { option ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { selectOption(option) }
                            .padding(all = 16.dp),
                    ) {
                        Text(
                            text = "[${option.statusCode}] ${option.description}",
                            fontSize = 14.sp
                        )
                        Text(text = "Response file: ${option.responseFile}", fontSize = 10.sp)
                    }
                }
            }
        }
    }

    if (allowCustomJson) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onToggleState,
                content = { Text(text = "Custom JSON", fontSize = 16.sp) }
            )
        }
    }
}

@Composable
private fun MockEngineChoiceScope.CustomJsonInputView(
    modifier: Modifier = Modifier,
    onToggleState: () -> Unit,
) = Column(modifier = modifier) {
    var json by remember { mutableStateOf("") }
    var statusCode by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.weight(1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            modifier = Modifier.weight(7f),
            value = json,
            onValueChange = { json = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Text,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next
            ),
            placeholder = {
                Text(text = "Insert your custom JSON here!", fontSize = 14.sp)
            },
        )

        TextField(
            modifier = Modifier.weight(3f),
            value = statusCode,
            onValueChange = { statusCode = it.filter(Char::isDigit) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Number,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done
            ),
            placeholder = {
                Text(text = "Status code", fontSize = 14.sp)
            },
        )
    }
    Spacer(modifier = Modifier.weight(1f))

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (optionList.isNotEmpty()) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onToggleState,
                content = { Text(text = "Option list", fontSize = 16.sp) }
            )
        }

        Button(
            modifier = Modifier.weight(1f),
            content = { Text(text = "Confirm", fontSize = 16.sp) },
            onClick = {
                selectOption(
                    MockOption.Custom(
                        statusCode = statusCode.toIntOrNull() ?: 200,
                        responseJson = json
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DefaultMockEngineChoiceView() {
    var isInsertingCustomJson by remember { mutableStateOf(false) }

    MockEngineChoiceView {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (isInsertingCustomJson || optionList.isEmpty()) {
                CustomJsonInputView(
                    modifier = Modifier.fillMaxWidth(),
                    onToggleState = { isInsertingCustomJson = false }
                )
            } else {
                OptionListView(
                    modifier = Modifier.fillMaxWidth(),
                    onToggleState = { isInsertingCustomJson = true }
                )
            }
        }
    }
}
