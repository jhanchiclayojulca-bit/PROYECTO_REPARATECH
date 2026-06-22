package com.cibertec.cibertecapp.features.home.presentation.state

import com.cibertec.cibertecapp.features.home.domain.model.Repair

data class HomeState(
    val repairs: List<Repair> = emptyList(),
    val isLoading: Boolean = true,
    val userName: String = "Usuario",
    val userPhotoUrl: String = "",
    val userPhone: String = "",
    val error: String? = null
)
