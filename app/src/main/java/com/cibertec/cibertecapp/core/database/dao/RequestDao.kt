package com.cibertec.cibertecapp.core.database.dao

import androidx.room.*
import com.cibertec.cibertecapp.core.database.entities.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsByUserId(userId: String): Flow<List<RequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<RequestEntity>)

    @Query("DELETE FROM requests WHERE userId = :userId")
    suspend fun deleteRequestsByUserId(userId: String)
}
