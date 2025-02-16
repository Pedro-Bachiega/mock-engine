package com.pedrobneto.mock.engine.client.resources

import com.pedrobneto.mock.engine.client.MockEngine

internal expect fun MockEngine.getFileContentFromResources(path: String): String?
