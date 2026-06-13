package com.cibertec.cibertecapp.features.profile.data.repository

import com.cibertec.cibertecapp.features.profile.domain.model.UserProfile
import com.cibertec.cibertecapp.features.profile.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl : ProfileRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun getProfile(): UserProfile {
        val user = auth.currentUser
        var name = user?.displayName ?: "Usuario"
        var email = user?.email ?: ""
        var avatar = user?.photoUrl?.toString() ?: ""
        var phone = "No registrado"
        var address = "No registrada"

        try {
            val doc = db.collection("users").document(user?.uid ?: "").get().await()
            if (doc.exists()) {
                name = doc.getString("name") ?: name
                phone = doc.getString("phone") ?: phone
                address = doc.getString("address") ?: address
                avatar = doc.getString("photoUrl") ?: avatar
            }
        } catch (e: Exception) { }

        return UserProfile(name, email, phone, address, avatar)
    }

    override suspend fun getRepairCount(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        return try {
            val snapshot = db.collection("repairs")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) { 0 }
    }

    // Añadimos también el conteo de equipos para el ViewModel
    suspend fun getDeviceCount(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        return try {
            val snapshot = db.collection("devices")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) { 0 }
    }

    override suspend fun updateProfile(name: String, phone: String, address: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
        return try {
            val data = hashMapOf(
                "name" to name,
                "phone" to phone,
                "address" to address
            )
            db.collection("users").document(userId).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
