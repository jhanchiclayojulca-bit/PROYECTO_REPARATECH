package com.cibertec.cibertecapp.features.profile.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.profile.data.repository.ProfileRepositoryImpl
import com.cibertec.cibertecapp.features.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val repository = ProfileRepositoryImpl()

    private val _state = MutableStateFlow<UserProfile?>(null)
    val state: StateFlow<UserProfile?> = _state.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            val profile = repository.getProfile()
            _state.value = profile
        }
    }

    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            val result = repository.updateProfile(name, phone, address)
            if (result.isSuccess) {
                _isSuccess.value = true
            }
        }
    }
}
