package com.pedrobneto.mock.engine.processor.model

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    PATCH;

    companion object {
        fun contains(value: String) = HttpMethod.entries.any { it.name == value }
    }
}
