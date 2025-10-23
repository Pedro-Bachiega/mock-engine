package com.pedrobneto.mockengine.network

import br.com.arch.toolkit.splinter.splinterExecuteRequestFlow

internal class SampleRepository(private val api: SampleApi) {
    fun sampleRequest(customPath: String) = splinterExecuteRequestFlow(id = "Sample") {
        api.sampleRequest(customPath)
    }
}