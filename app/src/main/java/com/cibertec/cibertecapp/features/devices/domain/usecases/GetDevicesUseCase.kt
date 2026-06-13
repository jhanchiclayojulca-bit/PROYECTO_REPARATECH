package com.cibertec.cibertecapp.features.devices.domain.usecases

import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.domain.repository.DeviceRepository

class GetDevicesUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(): List<Device> {
        return repository.getMyDevices()
    }
}
