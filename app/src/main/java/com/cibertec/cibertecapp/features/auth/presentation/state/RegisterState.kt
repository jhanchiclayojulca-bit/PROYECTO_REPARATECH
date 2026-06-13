package com.cibertec.cibertecapp.features.auth.presentation.state

data class RegisterState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
