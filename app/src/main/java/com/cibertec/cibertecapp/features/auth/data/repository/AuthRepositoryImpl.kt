package com.cibertec.cibertecapp.features.auth.data.repository

import com.cibertec.cibertecapp.features.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            // Si el usuario es nuevo, creamos su documento en Firestore
            if (user != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val currentDateTime = sdf.format(Date())

                val userMap = hashMapOf(
                    "uid" to user.uid,
                    "name" to (user.displayName ?: "Usuario Google"),
                    "email" to (user.email ?: ""),
                    "phone" to (user.phoneNumber ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "createdAt" to currentDateTime
                )

                // Usamos merge() para no sobreescribir datos si ya existía
                db.collection("users").document(user.uid)
                    .set(userMap, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun register(name: String, phone: String, email: String, pass: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user?.updateProfile(profileUpdates)?.await()

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val currentDateTime = sdf.format(Date())

            val userMap = hashMapOf(
                "uid" to user?.uid,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "photoUrl" to "",
                "createdAt" to currentDateTime
            )
            
            db.collection("users").document(user?.uid!!).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
