package com.cibertec.cibertecapp.features.repairs.presentation.state

import com.cibertec.cibertecapp.features.home.domain.model.Repair

data class RepairsState(
    val repairs: List<Repair> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)