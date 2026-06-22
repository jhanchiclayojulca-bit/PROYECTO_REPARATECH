package com.cibertec.cibertecapp.features.home.domain.repository

import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.domain.model.UserHomeProfile


interface HomeRepository {
    suspend fun getRepairs(): List<Repair>
    suspend fun getUserProfile(): UserHomeProfile
    fun searchRepairs(repairs: List<Repair>, query: String): List<Repair>
    fun filterRepairsByCategory(repairs: List<Repair>, category: String): List<Repair>
    suspend fun saveRepairsToLocal(repairs: List<Repair>)
    suspend fun getOfflineRepairs(): List<Repair>
    suspend fun getRepairById(repairId: String): Repair?
    suspend fun saveRepairRating(repairId: String, rating: Float, comment: String)
}
