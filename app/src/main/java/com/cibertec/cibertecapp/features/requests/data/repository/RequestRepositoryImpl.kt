package com.cibertec.cibertecapp.features.requests.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cibertec.cibertecapp.core.service.CloudinaryService
import com.cibertec.cibertecapp.core.database.AppDatabase
import com.cibertec.cibertecapp.core.database.entities.RequestEntity
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import com.cibertec.cibertecapp.features.requests.domain.repository.RequestRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class RequestRepositoryImpl(private val context: Context) : RequestRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val requestDao = AppDatabase.getDatabase(context).requestDao()

    override suspend fun getMyRequests(): List<QuotationRequest> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("requests")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val remoteRequests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(QuotationRequest::class.java)?.apply { id = doc.id }
            }.sortedByDescending { it.createdAt }

            // Sync
            val entities = remoteRequests.map { it.toEntity(userId) }
            requestDao.deleteRequestsByUserId(userId)
            requestDao.insertRequests(entities)

            remoteRequests
        } catch (e: Exception) {
            Log.e("RequestRepo", "Offline - Loading from Room")
            requestDao.getRequestsByUserId(userId).first().map { it.toDomain() }
        }
    }

    override suspend fun createRequest(request: QuotationRequest, imageUri: Uri?): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
            request.userId = userId

            if (request.photoUrl.isEmpty() && imageUri != null) {
                try {
                    val file = uriToFile(imageUri)
                    request.photoUrl = CloudinaryService.uploadImage(file)
                    file.delete()
                } catch (e: Exception) {
                    Log.e("RequestRepo", "Cloudinary upload failed: ${e.message}")
                }
            }

            db.collection("requests").document(request.id).set(request).await()
            
            // Sync
            requestDao.insertRequests(listOf(request.toEntity(userId)))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRequest(requestId: String): Result<Unit> {
        return try {
            db.collection("requests").document(requestId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun RequestEntity.toDomain() = QuotationRequest(
        id = id,
        userId = userId,
        deviceId = deviceId,
        brandAndModel = brandAndModel,
        problemDescription = problemDescription,
        status = status,
        estimatedPrice = estimatedPrice,
        adminComment = adminComment,
        photoUrl = photoUrl,
        createdAt = createdAt
    )

    private fun QuotationRequest.toEntity(userId: String) = RequestEntity(
        id = id,
        userId = userId,
        deviceId = deviceId,
        brandAndModel = brandAndModel,
        problemDescription = problemDescription,
        status = status,
        estimatedPrice = estimatedPrice,
        adminComment = adminComment,
        photoUrl = photoUrl,
        createdAt = createdAt
    )

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_request_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
