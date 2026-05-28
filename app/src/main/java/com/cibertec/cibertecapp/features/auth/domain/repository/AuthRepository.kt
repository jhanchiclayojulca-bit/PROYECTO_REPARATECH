package com.cibertec.cibertecapp.features.auth.domain.repository

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String
    ): Boolean
}