package com.pedrobneto.mockengine.network

import com.pedrobneto.mockengine.model.SampleModel
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

internal interface SampleApi {
    @GET("sample-url/{customPath}")
    fun sampleRequest(@Path("customPath") customPath: String): SampleModel
}
