package com.cibertec.cibertecapp.features.repairs.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cibertec.cibertecapp.core.service.CloudinaryService
import com.cibertec.cibertecapp.core.database.AppDatabase
import com.cibertec.cibertecapp.core.database.entities.RepairEntity
import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest
import com.cibertec.cibertecapp.features.repairs.domain.repository.RepairRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class RepairRepositoryImpl(private val context: Context) : RepairRepository {

    private val db = FirebaseFirestore.getInstance()
    private val repairDao = AppDatabase.getDatabase(context).repairDao()

    override suspend fun createRepair(request: RepairRequest, imageUri: Uri?): Result<Unit> {
        return try {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""
            request.userId = userId
            
            request.orderId = "TXN-${Random.nextInt(10000, 99999)}-RT"
            request.total = request.baseCost + request.tax + request.additionalCost
            
            var photoUrl = request.photoUrl
            imageUri?.let { uri ->
                try {
                    val file = uriToFile(uri)
                    photoUrl = CloudinaryService.uploadImage(file)
                    request.photoUrl = photoUrl
                    file.delete()
                } catch (e: Exception) {
                    Log.e("RepairRepo", "Cloudinary upload failed: ${e.message}")
                }
            }

            // 1. Firebase
            db.collection("repairs")
                .document(request.id)
                .set(request)
                .await()
            
            // 2. Room Sync
            repairDao.insertRepairs(listOf(request.toEntity(userId)))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RepairRepo", "Error crítico: ${e.message}")
            Result.failure(e)
        }
    }

    private fun RepairRequest.toEntity(userId: String) = RepairEntity(
        id = id,
        orderId = orderId,
        userId = userId,
        deviceId = deviceId,
        brandAndModel = brandAndModel,
        status = status,
        total = total,
        serviceType = deviceCategory,
        deliveryMethod = deliveryMethod,
        photoUrl = photoUrl,
        createdAt = createdAt
    )

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
