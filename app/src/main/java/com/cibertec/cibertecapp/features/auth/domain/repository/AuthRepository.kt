package com.cibertec.cibertecapp.features.auth.domain.repository

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String
    ): Boolean

    suspend fun loginWithGoogle(idToken: String): Boolean

    suspend fun register(name: String, phone: String, email: String, pass: String): Result<Unit>
}
