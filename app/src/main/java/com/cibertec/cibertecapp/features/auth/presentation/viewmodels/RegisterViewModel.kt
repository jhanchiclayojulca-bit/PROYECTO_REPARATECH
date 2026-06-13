package com.cibertec.cibertecapp.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.auth.data.repository.AuthRepositoryImpl
import com.cibertec.cibertecapp.features.auth.domain.usecases.RegisterUseCase
import com.cibertec.cibertecapp.features.auth.presentation.state.RegisterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepositoryImpl()
    private val registerUseCase = RegisterUseCase(repository)

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun register(name: String, phone: String, email: String, pass: String) {
        if (name.isBlank() || phone.isBlank() || email.isBlank() || pass.isBlank()) {
            _state.value = RegisterState(error = "Completa todos los campos")
            return
        }

        _state.value = RegisterState(isLoading = true)

        viewModelScope.launch {
            val result = registerUseCase(name, phone, email, pass)
            
            result.onSuccess {
                _state.value = RegisterState(isSuccess = true)
            }.onFailure { e ->
                _state.value = RegisterState(error = e.message)
            }
        }
    }
}
