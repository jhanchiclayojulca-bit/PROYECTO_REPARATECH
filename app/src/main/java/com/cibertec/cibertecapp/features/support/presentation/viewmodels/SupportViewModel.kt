package com.cibertec.cibertecapp.features.support.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.requests.data.repository.RequestRepositoryImpl
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SupportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RequestRepositoryImpl(application)

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendSupportRequest(deviceName: String, category: String, problemType: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = QuotationRequest(
                brandAndModel = deviceName,
                deviceCategory = category,
                problemDescription = "[$problemType] $description",
                status = "PENDIENTE"
            )
            val result = repository.createRequest(request, null)
            if (result.isSuccess) {
                _isSuccess.value = true
            }
            _isLoading.value = false
        }
    }
    
    fun resetSuccess() {
        _isSuccess.value = false
    }
}
