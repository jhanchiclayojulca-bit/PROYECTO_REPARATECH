package com.cibertec.cibertecapp.features.devices.presentation.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.devices.data.repository.DeviceRepositoryImpl
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.domain.usecases.AddDeviceUseCase
import com.cibertec.cibertecapp.features.devices.domain.usecases.GetDevicesUseCase
import com.cibertec.cibertecapp.features.devices.presentation.state.DevicesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DevicesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeviceRepositoryImpl(application)
    private val getDevicesUseCase = GetDevicesUseCase(repository)
    private val addDeviceUseCase = AddDeviceUseCase(repository)

    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private var allDevices = emptyList<Device>()
    private var currentCategory = ""
    private var currentQuery = ""

    fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            allDevices = getDevicesUseCase()
            _state.update { it.copy(isLoading = false, devices = allDevices) }
            applyFilters()
        }
    }

    fun searchDevices(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun filterByCategory(category: String) {
        currentCategory = if (currentCategory == category) "" else category
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allDevices

        if (currentCategory.isNotEmpty()) {
            filtered = filtered.filter { it.category.equals(currentCategory, ignoreCase = true) }
        }

        if (currentQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.brand.contains(currentQuery, ignoreCase = true) ||
                it.model.contains(currentQuery, ignoreCase = true) ||
                it.serialNumber.contains(currentQuery, ignoreCase = true)
            }
        }

        _state.update { it.copy(devices = filtered) }
    }

    fun updateImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun saveDevice(brand: String, model: String, serial: String, category: String, existingDevice: Device? = null) {
        if (brand.isBlank() || model.isBlank() || serial.isBlank() || category.isBlank()) {
            _state.update { it.copy(error = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // If existingDevice is provided, we preserve its ID and createdAt
            val device = existingDevice?.copy(
                brand = brand,
                model = model,
                serialNumber = serial,
                category = category
            ) ?: Device(
                brand = brand,
                model = model,
                serialNumber = serial,
                category = category
            )

            val result = addDeviceUseCase(device, _selectedImageUri.value)
            
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}
