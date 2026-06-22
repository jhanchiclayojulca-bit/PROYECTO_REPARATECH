package com.cibertec.cibertecapp.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repairs")
data class RepairEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val userId: String,
    val deviceId: String,
    val brandAndModel: String,
    val status: String,
    val total: Double,
    val serviceType: String,
    val deliveryMethod: String,
    val photoUrl: String,
    val problemDescription: String,
    val baseCost: Double,
    val tax: Double,
    val additionalCost: Double,
    val technician: String,
    val rating: Float,
    val ratingComment: String,
    val isPaid: Boolean,
    val createdAt: Long
)
