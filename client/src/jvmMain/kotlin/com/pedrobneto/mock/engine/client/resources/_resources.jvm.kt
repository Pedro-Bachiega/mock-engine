package com.pedrobneto.mock.engine.client.resources

import java.io.BufferedReader

internal actual fun Any.getFileContentFromResources(path: String): String? =
    this::class.java.getResourceAsStream(path)?.bufferedReader()?.use(BufferedReader::readText)
