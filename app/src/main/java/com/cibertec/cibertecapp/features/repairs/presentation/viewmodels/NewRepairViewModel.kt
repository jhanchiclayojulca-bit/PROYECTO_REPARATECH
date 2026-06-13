package com.cibertec.cibertecapp.features.repairs.presentation.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.devices.data.repository.DeviceRepositoryImpl
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.domain.usecases.GetDevicesUseCase
import com.cibertec.cibertecapp.features.profile.data.repository.ProfileRepositoryImpl
import com.cibertec.cibertecapp.features.repairs.data.repository.RepairRepositoryImpl
import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest
import com.cibertec.cibertecapp.features.repairs.domain.usecases.CreateRepairUseCase
import com.cibertec.cibertecapp.features.repairs.presentation.state.NewRepairState
import com.cibertec.cibertecapp.features.requests.data.repository.RequestRepositoryImpl
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewRepairViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RepairRepositoryImpl(application)
    private val deviceRepository = DeviceRepositoryImpl(application)
    private val requestRepository = RequestRepositoryImpl(application)
    private val profileRepository = ProfileRepositoryImpl()
    private val createRepairUseCase = CreateRepairUseCase(repository)
    private val getDevicesUseCase = GetDevicesUseCase(deviceRepository)

    private val _state = MutableStateFlow(NewRepairState())
    val state: StateFlow<NewRepairState> = _state

    private val _myDevices = MutableStateFlow<List<Device>>(emptyList())
    val myDevices: StateFlow<List<Device>> = _myDevices

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep

    var isQuotationOnly = false
    var shouldStartAtStep2 = false

    init {
        loadMyDevices()
    }

    fun loadMyDevices() {
        viewModelScope.launch {
            _myDevices.value = getDevicesUseCase()
        }
    }

    fun loadFromQuotation(quotation: QuotationRequest) {
        shouldStartAtStep2 = true
        _state.update {
            it.copy(
                isSuccess = false,
                isLoading = false,
                error = null,
                request = it.request.copy(
                    deviceId = quotation.deviceId,
                    deviceCategory = quotation.deviceCategory,
                    brandAndModel = quotation.brandAndModel,
                    serialNumber = quotation.serialNumber,
                    problemDescription = quotation.problemDescription,
                    photoUrl = quotation.photoUrl,
                    baseCost = quotation.estimatedPrice
                )
            )
        }
        _currentStep.value = 2
    }

    fun submitQuotation() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val req = _state.value.request
            val quotation = QuotationRequest(
                deviceId = req.deviceId,
                deviceCategory = req.deviceCategory,
                brandAndModel = req.brandAndModel,
                serialNumber = req.serialNumber,
                problemDescription = req.problemDescription,
                photoUrl = req.photoUrl 
            )
            val result = requestRepository.createRequest(quotation, _selectedImageUri.value)
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

    fun selectDevice(device: Device) {
        updateCategory(device.category)
        _state.update {
            it.copy(
                request = it.request.copy(
                    deviceId = device.id,
                    deviceCategory = device.category,
                    brandAndModel = "${device.brand} ${device.model}",
                    serialNumber = device.serialNumber,
                    photoUrl = device.photoUrl
                )
            )
        }
    }

    fun updateImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun nextStep() {
        if (_currentStep.value < 3) _currentStep.value++
    }

    fun previousStep() {
        if (_currentStep.value > 1) _currentStep.value--
    }

    fun updateStep(step: Int) {
        _currentStep.value = step
    }

    fun updateCategory(category: String) {
        _state.update {
            val base = when (category) {
                "Smartphone" -> 85.0
                "Laptop" -> 165.0
                "Tablet" -> 110.0
                "Gaming" -> 140.0
                else -> 100.0
            }
            val tax = base * 0.18
            it.copy(
                request = it.request.copy(
                    deviceCategory = category,
                    baseCost = base,
                    tax = tax,
                    total = base + tax + it.request.additionalCost
                )
            )
        }
    }

    fun updateStep1Details(model: String, serial: String, description: String) {
        _state.update {
            it.copy(
                request = it.request.copy(
                    brandAndModel = model,
                    serialNumber = serial,
                    problemDescription = description
                )
            )
        }
    }
    
    fun updateDeliveryMethod(method: String) {
        _state.update {
            it.copy(request = it.request.copy(deliveryMethod = method))
        }
    }

    fun updatePaymentMethod(method: String) {
        _state.update {
            it.copy(request = it.request.copy(paymentMethod = method))
        }
    }

    fun updateServiceType(isExpress: Boolean) {
        _state.update {
            val additional = if (isExpress) 35.0 else 0.0
            it.copy(
                request = it.request.copy(
                    serviceType = if (isExpress) "Express" else "Estándar",
                    additionalCost = additional,
                    total = it.request.baseCost + it.request.tax + additional
                )
            )
        }
    }

    fun confirmOrder() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val currentReq = _state.value.request
                val finalRequest = currentReq.copy(
                    userId = currentUser?.uid ?: "",
                    total = currentReq.baseCost + currentReq.tax + currentReq.additionalCost
                )
                val result = createRepairUseCase(finalRequest, _selectedImageUri.value)
                result.onSuccess {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }.onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    suspend fun getUserAddress(): String {
        return profileRepository.getProfile().address
    }

    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            profileRepository.updateProfile(profile.name, profile.phone, newAddress)
        }
    }
}
