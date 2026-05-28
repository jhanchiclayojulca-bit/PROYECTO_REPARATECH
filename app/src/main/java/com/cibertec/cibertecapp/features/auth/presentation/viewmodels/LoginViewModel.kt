package com.cibertec.cibertecapp.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.auth.data.repository.AuthRepositoryImpl
import com.cibertec.cibertecapp.features.auth.domain.usecases.LoginUseCase
import com.cibertec.cibertecapp.features.auth.presentation.state.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = AuthRepositoryImpl()
    private val loginUseCase = LoginUseCase(repository)
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(
        email: String,
        password: String
    ){
        viewModelScope.launch {
            _state.value = LoginState(
                isLoading = true
            )
            try {
                val success = loginUseCase(
                    email,
                    password
                )
                if (success){
                    _state.value = LoginState(
                        isSuccess = true
                    )
                } else {
                  _state.value = LoginState(
                      error = "Credenciales incorrectas"
                  )
                }
            }catch (e: Exception){
                _state.value = LoginState(
                    error = e.message
                )
            }
        }
    }
}