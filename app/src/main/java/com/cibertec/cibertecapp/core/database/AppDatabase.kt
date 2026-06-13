package com.cibertec.cibertecapp.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cibertec.cibertecapp.core.database.dao.DeviceDao
import com.cibertec.cibertecapp.core.database.dao.RepairDao
import com.cibertec.cibertecapp.core.database.dao.RequestDao
import com.cibertec.cibertecapp.core.database.entities.DeviceEntity
import com.cibertec.cibertecapp.core.database.entities.RepairEntity
import com.cibertec.cibertecapp.core.database.entities.RequestEntity

@Database(
    entities = [DeviceEntity::class, RepairEntity::class, RequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun repairDao(): RepairDao
    abstract fun requestDao(): RequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reparatech_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
