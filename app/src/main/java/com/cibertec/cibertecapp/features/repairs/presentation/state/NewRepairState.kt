package com.cibertec.cibertecapp.features.repairs.presentation.state

import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest

data class NewRepairState(
    val request: RepairRequest = RepairRequest(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
