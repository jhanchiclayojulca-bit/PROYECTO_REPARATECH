package com.cibertec.cibertecapp.features.repairs.domain.repository

import android.net.Uri
import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest

interface RepairRepository {
    suspend fun createRepair(request: RepairRequest, imageUri: Uri?): Result<Unit>
}
