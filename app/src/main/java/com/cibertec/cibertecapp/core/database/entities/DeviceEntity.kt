package com.cibertec.cibertecapp.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
    val category: String,
    val photoUrl: String,
    val status: String,
    val createdAt: Long
)
