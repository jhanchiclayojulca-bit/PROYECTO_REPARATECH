package com.cibertec.cibertecapp.features.profile.domain.repository

import com.cibertec.cibertecapp.features.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun getRepairCount(): Int
    suspend fun updateProfile(name: String, phone: String, address: String): Result<Unit>
    fun logout()
}
