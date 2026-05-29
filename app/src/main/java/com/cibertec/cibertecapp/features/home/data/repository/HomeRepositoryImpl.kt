package com.cibertec.cibertecapp.features.home.data.repository

import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.features.home.data.model.RepairModel
import com.cibertec.cibertecapp.features.home.domain.repository.HomeRepository

class HomeRepositoryImpl : HomeRepository {
    override fun getRepairs(): List<RepairModel> {

        return listOf(
            RepairModel(
                deviceName = "MacBook Pro M2",
                orderId = "ORD-1001",
                status = "PROGRESO",
                progress = 35,
                date = "24 Oct",
                iconRes = R.drawable.ic_laptop,
                category = "Laptops"
            ),

            RepairModel(
                deviceName = "iPhone 15",
                orderId = "ORD-1002",
                status = "REVISIÓN",
                progress = 70,
                date = "20 Oct",
                iconRes = R.drawable.ic_smartphone,
                category = "Celulares"
            ),

            RepairModel(
                deviceName = "PlayStation 5",
                orderId = "ORD-1003",
                status = "COMPLETADO",
                progress = 100,
                date = "18 Oct",
                iconRes = R.drawable.ic_gaming,
                category = "Gaming"
            )
        )
    }
}