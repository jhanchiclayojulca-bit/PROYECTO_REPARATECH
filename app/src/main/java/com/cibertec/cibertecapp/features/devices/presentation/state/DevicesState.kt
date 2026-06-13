package com.cibertec.cibertecapp.features.devices.presentation.state

import com.cibertec.cibertecapp.features.devices.domain.model.Device

data class DevicesState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
