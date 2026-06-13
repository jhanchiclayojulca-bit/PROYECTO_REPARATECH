package com.cibertec.cibertecapp.features.devices.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Device(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var brand: String = "",
    var model: String = "",
    var serialNumber: String = "",
    var category: String = "",
    var photoUrl: String = "",
    var status: String = "Activo",
    var createdAt: Long = System.currentTimeMillis()
) : Parcelable
