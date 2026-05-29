package com.cibertec.cibertecapp.features.auth.data.repository

import com.cibertec.cibertecapp.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Boolean {
        return email == "client@gmail.com"
                && password == "Client@12"
    }

}