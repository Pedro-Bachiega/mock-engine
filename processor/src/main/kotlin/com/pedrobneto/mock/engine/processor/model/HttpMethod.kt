package com.pedrobneto.mock.engine.processor.model

internal enum class HttpMethod {
    Get,
    Post,
    Put,
    Delete,
    Head,
    Patch;

    companion object {
        fun contains(value: String) =
            HttpMethod.entries.any { it.name.lowercase() == value.lowercase() }
    }
}
