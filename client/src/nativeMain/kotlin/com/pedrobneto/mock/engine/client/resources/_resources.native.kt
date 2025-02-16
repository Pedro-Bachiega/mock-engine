package com.pedrobneto.mock.engine.client.resources

import com.pedrobneto.mock.engine.client.MockEngine
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
internal actual fun MockEngine.getFileContentFromResources(path: String): String? {
    val filePath = NSBundle.mainBundle
        .pathForResource("compose-resources/json/$path", ofType = "json")
        ?: return null
    return NSString.stringWithContentsOfFile(
        filePath,
        encoding = NSUTF8StringEncoding,
        error = null
    )
}
