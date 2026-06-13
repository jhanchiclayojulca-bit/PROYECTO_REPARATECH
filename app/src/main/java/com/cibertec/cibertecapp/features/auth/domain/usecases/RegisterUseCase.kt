package com.cibertec.cibertecapp.features.auth.domain.usecases

import com.cibertec.cibertecapp.features.auth.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, phone: String, email: String, pass: String): Result<Unit> {
        return repository.register(name, phone, email, pass)
    }
}
