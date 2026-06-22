package com.cibertec.cibertecapp.features.devices.domain.repository

import android.net.Uri
import com.cibertec.cibertecapp.features.devices.domain.model.Device

interface DeviceRepository {
    suspend fun getMyDevices(): List<Device>
    suspend fun addDevice(device: Device, imageUri: Uri?): Result<Unit>
    suspend fun deleteDevice(deviceId: String): Result<Unit>
    suspend fun saveDevicesToLocal(devices: List<Device>)
    suspend fun getOfflineDevices(): List<Device>
}
