package com.cibertec.cibertecapp.core.database.dao

import androidx.room.*
import com.cibertec.cibertecapp.core.database.entities.RepairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {
    @Query("SELECT * FROM repairs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRepairsByUserId(userId: String): Flow<List<RepairEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairs(repairs: List<RepairEntity>)

    @Query("DELETE FROM repairs WHERE userId = :userId")
    suspend fun deleteRepairsByUserId(userId: String)
}
