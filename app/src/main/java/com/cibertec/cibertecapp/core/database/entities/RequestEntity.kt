package com.cibertec.cibertecapp.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val deviceId: String,
    val brandAndModel: String,
    val problemDescription: String,
    val status: String,
    val estimatedPrice: Double,
    val adminComment: String,
    val photoUrl: String,
    val createdAt: Long
)
