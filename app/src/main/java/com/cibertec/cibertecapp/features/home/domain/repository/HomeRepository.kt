package com.cibertec.cibertecapp.features.home.domain.repository

import com.cibertec.cibertecapp.features.home.data.model.RepairModel

interface HomeRepository {

    fun getRepairs(): List<RepairModel>
}