package com.cibertec.cibertecapp.features.auth.domain.usecases

import com.cibertec.cibertecapp.features.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Boolean{
        return repository.login(
            email,
            password
        )
    }
}