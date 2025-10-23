package com.pedrobneto.mock.engine.client.view

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrobneto.mock.engine.client.model.FileOptions
import com.pedrobneto.mock.engine.client.model.MockOption
import kotlinx.coroutines.launch

@Composable
private fun OptionListView(
    modifier: Modifier = Modifier,
    fileOptions: List<FileOptions>,
    onOptionSelected: (option: MockOption.Default) -> Unit
) {
    Column(modifier = modifier) {
        fileOptions.forEach { fileOption ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    text = "Mock file: ${fileOption.fileName}",
                    fontSize = 12.sp
                )

                fileOption.options.forEach { option ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 16.dp)
                            .clickable { onOptionSelected.invoke(option) },
                        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
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
}

@Composable
private fun CustomJsonInputView(
    modifier: Modifier = Modifier,
    onOptionSelected: (option: MockOption) -> Unit
) {
    var json by remember { mutableStateOf("") }
    var statusCode by remember { mutableStateOf("") }

    Column(modifier = modifier) {
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
                onValueChange = { statusCode = it },
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

        Button(onClick = {
            onOptionSelected.invoke(
                MockOption.Custom(
                    statusCode = statusCode.toIntOrNull() ?: 200,
                    responseJson = json
                )
            )
        }) {
            Text(text = "Confirm", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DefaultMockEngineChoiceView() {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var isInsertingCustomJson by remember { mutableStateOf(false) }

    MockEngineChoiceView { allowCustomJson, dataList, onOptionSelected ->
        ModalBottomSheet(
            modifier = Modifier.padding(bottom = 16.dp),
            sheetState = sheetState,
            onDismissRequest = {
                onOptionSelected.invoke(
                    dataList.flatMap(FileOptions::options).first()
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .animateContentSize(alignment = Alignment.BottomCenter)
            ) {
                if (allowCustomJson) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { isInsertingCustomJson = !isInsertingCustomJson }) {
                            Text(
                                text = if (isInsertingCustomJson) "Option list" else "Custom JSON",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                fun selectOption(option: MockOption) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onOptionSelected.invoke(option)
                    }
                }

                if (isInsertingCustomJson) {
                    CustomJsonInputView(
                        modifier = Modifier.fillMaxWidth(),
                        onOptionSelected = ::selectOption
                    )
                } else {
                    OptionListView(
                        modifier = Modifier.fillMaxWidth(),
                        fileOptions = dataList,
                        onOptionSelected = ::selectOption
                    )
                }
            }
        }
    }
}
