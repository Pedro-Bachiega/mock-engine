package com.pedrobneto.mockengine.model

import kotlinx.serialization.Serializable

@Serializable
data class SampleModel(
    val title: String,
    val description: String
)
