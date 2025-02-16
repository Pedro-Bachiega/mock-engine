package com.pedrobneto.mock.engine.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MockData(val options: List<Option>) {
    @Serializable
    data class Option(
        val description: String,
        @SerialName("status_code") val statusCode: Int,
        @SerialName("response_file") val responseFile: String? = null
    )
}
