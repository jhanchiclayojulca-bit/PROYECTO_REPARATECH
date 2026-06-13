package com.cibertec.cibertecapp.core.database.dao

import androidx.room.*
import com.cibertec.cibertecapp.core.database.entities.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices WHERE userId = :userId")
    fun getDevicesByUserId(userId: String): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE userId = :userId")
    suspend fun deleteDevicesByUserId(userId: String)
}
