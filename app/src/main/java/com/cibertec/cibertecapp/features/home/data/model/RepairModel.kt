package com.cibertec.cibertecapp.features.home.data.model

data class RepairModel (

    val deviceName: String,
    val orderId: String,
    val status: String,
    val progress: Int,
    val date: String,
    val iconRes: Int,
    val category: String
)