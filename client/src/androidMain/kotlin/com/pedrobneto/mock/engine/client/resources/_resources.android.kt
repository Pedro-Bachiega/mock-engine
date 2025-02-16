package com.pedrobneto.mock.engine.client.resources

import com.pedrobneto.mock.engine.client.MockEngine
import java.io.BufferedReader

internal actual fun MockEngine.getFileContentFromResources(path: String): String? =
    this::class.java.getResourceAsStream(path)?.bufferedReader()?.use(BufferedReader::readText)
