package com.cibertec.cibertecapp.features.home.presentation.state

import com.cibertec.cibertecapp.features.home.data.model.RepairModel

data class HomeState(

    val  repairs: List<RepairModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)