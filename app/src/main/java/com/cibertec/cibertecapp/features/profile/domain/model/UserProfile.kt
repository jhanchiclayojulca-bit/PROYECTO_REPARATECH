package com.cibertec.cibertecapp.features.profile.domain.model

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val avatarUrl: String
)
