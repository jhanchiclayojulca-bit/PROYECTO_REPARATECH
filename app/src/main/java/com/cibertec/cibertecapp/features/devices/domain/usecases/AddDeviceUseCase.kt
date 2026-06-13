package com.cibertec.cibertecapp.features.devices.domain.usecases

import android.net.Uri
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.domain.repository.DeviceRepository

class AddDeviceUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(device: Device, imageUri: Uri?): Result<Unit> {
        return repository.addDevice(device, imageUri)
    }
}
