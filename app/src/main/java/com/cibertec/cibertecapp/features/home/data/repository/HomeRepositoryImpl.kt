package com.cibertec.cibertecapp.features.home.data.repository

import android.content.Context
import com.cibertec.cibertecapp.core.database.AppDatabase
import com.cibertec.cibertecapp.core.database.entities.RepairEntity
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.domain.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeRepositoryImpl(private val context: Context) : HomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repairDao = AppDatabase.getDatabase(context).repairDao()

    override suspend fun getUserProfile(): com.cibertec.cibertecapp.features.home.domain.model.UserHomeProfile {
        val user = auth.currentUser
        return if (user != null) {
            var name = user.displayName ?: "Usuario"
            var photo = user.photoUrl?.toString() ?: ""
            var phone = user.phoneNumber ?: ""

            try {
                val doc = db.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    name = doc.getString("name") ?: name
                    photo = doc.getString("photoUrl") ?: photo
                    phone = doc.getString("phone") ?: phone
                }
            } catch (e: Exception) { }
            com.cibertec.cibertecapp.features.home.domain.model.UserHomeProfile(name, photo, phone)
        } else {
            com.cibertec.cibertecapp.features.home.domain.model.UserHomeProfile("Invitado", "", "")
        }
    }

    override suspend fun getRepairs(): List<Repair> {
        val currentUser = auth.currentUser ?: return emptyList()
        return try {
            val snapshot = db.collection("repairs")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val remoteRepairs = snapshot.documents.map { doc ->
                val createdAt = doc.getLong("createdAt") ?: 0L
                val dateFormatted = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(createdAt))
                val categoryFromDb = doc.getString("deviceCategory") ?: ""
                val normalizedCategory = if (categoryFromDb.equals("Otros", true)) "Gaming" else categoryFromDb

                Repair(
                    id = doc.id,
                    deviceId = doc.getString("deviceId") ?: "",
                    deviceName = doc.getString("brandAndModel") ?: "Desconocido",
                    orderId = doc.getString("orderId") ?: "N/A",
                    status = doc.getString("status") ?: "PENDIENTE",
                    progress = when(doc.getString("status")?.uppercase()) {
                        "COMPLETADO" -> 100
                        "PROGRESO" -> 50
                        "REVISIÓN" -> 25
                        else -> 10
                    },
                    date = dateFormatted,
                    photoUrl = doc.getString("photoUrl") ?: "",
                    category = normalizedCategory,
                    service = doc.getString("problemDescription") ?: "Sin descripción",
                    deliveryMethod = doc.getString("deliveryMethod") ?: "Presencial",
                    technician = doc.getString("technician") ?: "Técnico ReparaTech",
                    baseCost = doc.getDouble("baseCost") ?: 0.0,
                    tax = doc.getDouble("tax") ?: 0.0,
                    additionalCost = doc.getDouble("additionalCost") ?: 0.0,
                    total = doc.getDouble("total") ?: 0.0,
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    ratingComment = doc.getString("ratingComment") ?: "",
                    isPaid = doc.getBoolean("isPaid") ?: false,
                    createdAt = createdAt
                )
            }.sortedByDescending { it.createdAt }

            // Sync
            saveRepairsToLocal(remoteRepairs)

            remoteRepairs
        } catch (e: Exception) {
            android.util.Log.e("HomeRepo", "Offline - Loading from Room")
            getOfflineRepairs()
        }
    }

    override suspend fun getOfflineRepairs(): List<Repair> {
        val currentUser = auth.currentUser ?: return emptyList()
        return repairDao.getRepairsByUserId(currentUser.uid).first().map { it.toDomain() }
    }

    override suspend fun getRepairById(repairId: String): Repair? {

        val cached = repairDao.getRepairById(repairId)?.toDomain()
        
        // 2. Intentar actualizar desde Firebase si hay internet
        return try {
            val doc = db.collection("repairs").document(repairId).get().await()
            if (doc.exists()) {
                val createdAt = doc.getLong("createdAt") ?: 0L
                val remote = Repair(
                    id = doc.id,
                    deviceId = doc.getString("deviceId") ?: "",
                    deviceName = doc.getString("brandAndModel") ?: "Desconocido",
                    orderId = doc.getString("orderId") ?: "N/A",
                    status = doc.getString("status") ?: "PENDIENTE",
                    progress = when(doc.getString("status")?.uppercase()) {
                        "COMPLETADO" -> 100
                        "PROGRESO" -> 50
                        "REVISIÓN" -> 25
                        else -> 10
                    },
                    date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(createdAt)),
                    photoUrl = doc.getString("photoUrl") ?: "",
                    category = doc.getString("deviceCategory") ?: "",
                    service = doc.getString("problemDescription") ?: "Sin descripción",
                    deliveryMethod = doc.getString("deliveryMethod") ?: "Presencial",
                    technician = doc.getString("technician") ?: "Técnico ReparaTech",
                    baseCost = doc.getDouble("baseCost") ?: 0.0,
                    tax = doc.getDouble("tax") ?: 0.0,
                    additionalCost = doc.getDouble("additionalCost") ?: 0.0,
                    total = doc.getDouble("total") ?: 0.0,
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    ratingComment = doc.getString("ratingComment") ?: "",
                    isPaid = doc.getBoolean("isPaid") ?: false,
                    createdAt = createdAt
                )
                // Guardar en Room la versión actualizada
                val userId = auth.currentUser?.uid ?: ""
                repairDao.insertRepairs(listOf(remote.toEntity(userId)))
                remote
            } else cached
        } catch (e: Exception) {
            cached // Si no hay internet, devolvemos lo que tenemos en Room
        }
    }

    override suspend fun saveRepairsToLocal(repairs: List<Repair>) {
        val currentUser = auth.currentUser ?: return
        val entities = repairs.map { it.toEntity(currentUser.uid) }
        repairDao.deleteRepairsByUserId(currentUser.uid)
        repairDao.insertRepairs(entities)
    }

    override suspend fun saveRepairRating(repairId: String, rating: Float, comment: String) {
        try {
            db.collection("repairs").document(repairId)
                .update(mapOf(
                    "rating" to rating,
                    "ratingComment" to comment
                )).await()
        } catch (e: Exception) {
            android.util.Log.e("HomeRepo", "Error saving rating: ${e.message}")
            throw e
        }
    }

    private fun RepairEntity.toDomain() = Repair(
        id = id,
        orderId = orderId,
        deviceId = deviceId,
        deviceName = brandAndModel,
        status = status,
        total = total,
        category = serviceType,
        deliveryMethod = deliveryMethod,
        photoUrl = photoUrl,
        service = problemDescription,
        baseCost = baseCost,
        tax = tax,
        additionalCost = additionalCost,
        technician = technician,
        rating = rating,
        ratingComment = ratingComment,
        isPaid = isPaid,
        createdAt = createdAt,
        date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(createdAt)),
        progress = when(status.uppercase()) {
            "COMPLETADO" -> 100
            "PROGRESO" -> 50
            "REVISIÓN" -> 25
            else -> 10
        }
    )

    private fun Repair.toEntity(userId: String) = RepairEntity(
        id = id,
        orderId = orderId,
        userId = userId,
        deviceId = deviceId,
        brandAndModel = deviceName,
        status = status,
        total = total,
        serviceType = category,
        deliveryMethod = deliveryMethod,
        photoUrl = photoUrl,
        problemDescription = service,
        baseCost = baseCost,
        tax = tax,
        additionalCost = additionalCost,
        technician = technician,
        rating = rating,
        ratingComment = ratingComment,
        isPaid = isPaid,
        createdAt = createdAt
    )

    override fun searchRepairs(repairs: List<Repair>, query: String): List<Repair> {
        return repairs.filter {
            it.deviceName.contains(query, true) ||
            it.service.contains(query, true) ||
            it.orderId.contains(query, true)
        }
    }

    override fun filterRepairsByCategory(repairs: List<Repair>, category: String): List<Repair> {
        return repairs.filter {
            it.category.equals(category, true)
        }
    }
}
