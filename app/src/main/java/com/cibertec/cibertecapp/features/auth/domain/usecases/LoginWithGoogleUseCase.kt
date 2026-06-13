package com.cibertec.cibertecapp.features.auth.domain.usecases

import com.cibertec.cibertecapp.features.auth.domain.repository.AuthRepository

class LoginWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Boolean {
        return repository.loginWithGoogle(idToken)
    }
}
