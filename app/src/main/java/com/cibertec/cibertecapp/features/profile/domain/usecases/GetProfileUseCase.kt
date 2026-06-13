package com.cibertec.cibertecapp.features.profile.domain.usecases

import com.cibertec.cibertecapp.features.profile.domain.model.UserProfile
import com.cibertec.cibertecapp.features.profile.domain.repository.ProfileRepository

class GetProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): UserProfile {
        return repository.getProfile()
    }
}
