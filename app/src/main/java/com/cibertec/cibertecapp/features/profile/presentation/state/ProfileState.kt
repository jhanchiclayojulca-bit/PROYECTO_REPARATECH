package com.cibertec.cibertecapp.features.profile.presentation.state

data class ProfileState(
    val isLoading: Boolean = false,
    val name: String = "Cargando...",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val totalRepairs: Int = 0,
    val totalDevices: Int = 0,
    val isLoggedOut: Boolean = false,
    val error: String? = null
)
