package com.cibertec.cibertecapp.features.repairs.domain.usecases

import android.net.Uri
import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest
import com.cibertec.cibertecapp.features.repairs.domain.repository.RepairRepository

class CreateRepairUseCase(private val repository: RepairRepository) {
    suspend operator fun invoke(request: RepairRequest, imageUri: Uri?): Result<Unit> {
        return repository.createRepair(request, imageUri)
    }
}
