package com.pedrobneto.mockengine.network

import com.pedrobneto.mock.engine.annotation.Mock
import com.pedrobneto.mockengine.model.SampleModel
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

internal interface SampleApi {
    @Mock(files = ["mock/sample-mock.json"], allowCustomJson = true)
    @GET("sample-url/{customPath}")
    suspend fun sampleRequest(@Path("customPath") customPath: String): SampleModel
}
