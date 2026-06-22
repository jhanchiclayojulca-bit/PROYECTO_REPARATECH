package com.cibertec.cibertecapp.features.devices.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cibertec.cibertecapp.core.service.CloudinaryService
import com.cibertec.cibertecapp.core.database.AppDatabase
import com.cibertec.cibertecapp.core.database.entities.DeviceEntity
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.domain.repository.DeviceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val deviceDao = AppDatabase.getDatabase(context).deviceDao()

    override suspend fun getMyDevices(): List<Device> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        
        return try {
            val snapshot = db.collection("devices")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val remoteDevices = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Device::class.java)?.apply { id = doc.id }
            }

            // Sync with Room
            saveDevicesToLocal(remoteDevices)

            remoteDevices
        } catch (e: Exception) {
            Log.e("DeviceRepo", "Offline mode - Fetching from Room")
            getOfflineDevices()
        }
    }

    override suspend fun saveDevicesToLocal(devices: List<Device>) {
        val userId = auth.currentUser?.uid ?: return
        val entities = devices.map { it.toEntity(userId) }
        deviceDao.deleteDevicesByUserId(userId)
        deviceDao.insertDevices(entities)
    }

    override suspend fun getOfflineDevices(): List<Device> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return deviceDao.getDevicesByUserId(userId).first().map { it.toDomain() }
    }

    override suspend fun addDevice(device: Device, imageUri: Uri?): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        
        return try {
            var photoUrl = device.photoUrl
            imageUri?.let { uri ->
                try {
                    val file = uriToFile(uri)
                    photoUrl = CloudinaryService.uploadImage(file)
                    file.delete()
                } catch (e: Exception) {
                    Log.e("DeviceRepo", "Cloudinary upload failed: ${e.message}")
                }
            }

            val deviceData = hashMapOf(
                "id" to device.id,
                "userId" to userId,
                "brand" to device.brand,
                "model" to device.model,
                "serialNumber" to device.serialNumber,
                "category" to device.category,
                "photoUrl" to photoUrl,
                "status" to device.status,
                "createdAt" to device.createdAt
            )

            db.collection("devices").document(device.id).set(deviceData).await()
            
            // Sync Room
            deviceDao.insertDevices(listOf(device.copy(photoUrl = photoUrl).toEntity(userId)))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return try {
            db.collection("devices").document(deviceId).delete().await()
            // Podríamos borrar de Room también
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DeviceEntity.toDomain() = Device(
        id = id,
        userId = userId,
        brand = brand,
        model = model,
        serialNumber = serialNumber,
        category = category,
        photoUrl = photoUrl,
        status = status,
        createdAt = createdAt
    )

    private fun Device.toEntity(userId: String) = DeviceEntity(
        id = id,
        userId = userId,
        brand = brand,
        model = model,
        serialNumber = serialNumber,
        category = category,
        photoUrl = photoUrl,
        status = status,
        createdAt = createdAt
    )

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_device_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
