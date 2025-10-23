package com.pedrobneto.mockengine.ui

import androidx.lifecycle.ViewModel
import br.com.arch.toolkit.result.DataResult
import br.com.arch.toolkit.util.dataResultSuccess
import com.pedrobneto.mockengine.model.SampleModel
import com.pedrobneto.mockengine.network.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SampleViewModel internal constructor(private val repository: SampleRepository) : ViewModel() {
    private val _sampleFlow = MutableStateFlow<DataResult<SampleModel>>(dataResultSuccess(null))
    val sampleFlow: StateFlow<DataResult<SampleModel>> get() = _sampleFlow

    suspend fun fetchSampleModel(customPath: String) {
        repository.sampleRequest(customPath).collect(_sampleFlow)
    }
}