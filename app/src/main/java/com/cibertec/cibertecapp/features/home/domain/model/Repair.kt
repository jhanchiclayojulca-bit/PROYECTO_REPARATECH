package com.cibertec.cibertecapp.features.home.domain.model

data class Repair (
    val id: String = "",
    val deviceId: String = "", // Vínculo con el equipo
    val deviceName: String = "",
    val orderId: String = "",
    val status: String = "PENDIENTE",
    val progress: Int = 0,
    val date: String = "",
    val photoUrl: String = "",
    val category: String = "",
    val service: String = "",
    val deliveryMethod: String = "Presencial", // Nuevo campo necesario
    val technician: String = "Asignando...",
    val baseCost: Double = 0.0,
    val tax: Double = 0.0, // Campo para impuestos
    val additionalCost: Double = 0.0,
    val total: Double = 0.0,
    val createdAt: Long = 0L // Nuevo campo para ordenar bien
)
