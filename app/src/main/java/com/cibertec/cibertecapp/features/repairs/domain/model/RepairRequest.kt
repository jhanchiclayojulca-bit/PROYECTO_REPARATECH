package com.cibertec.cibertecapp.features.repairs.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class RepairRequest(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var deviceId: String = "", // Para vincular con el equipo
    var orderId: String = "",
    var deviceCategory: String = "",
    var brandAndModel: String = "",
    var serialNumber: String = "",
    var problemDescription: String = "",
    var serviceType: String = "Estándar",
    var deliveryMethod: String = "Presencial",
    var paymentMethod: String = "Tarjeta", // Nuevo campo
    var baseCost: Double = 115.0,
    var tax: Double = 15.0,
    var additionalCost: Double = 0.0,
    var total: Double = 0.0,
    var status: String = "Pendiente",
    var createdAt: Long = System.currentTimeMillis(),
    var photoUrl: String = ""
) : Parcelable
