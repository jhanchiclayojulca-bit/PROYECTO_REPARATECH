package com.cibertec.cibertecapp.features.requests.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class QuotationRequest(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var deviceId: String = "", // Opcional si es un equipo guardado
    var deviceCategory: String = "",
    var brandAndModel: String = "",
    var serialNumber: String = "",
    var problemDescription: String = "",
    var photoUrl: String = "",
    var status: String = "PENDIENTE", // PENDIENTE, COTIZADO, RECHAZADO, ACEPTADO
    var estimatedPrice: Double = 0.0,
    var adminComment: String = "",
    var createdAt: Long = System.currentTimeMillis()
) : Parcelable
