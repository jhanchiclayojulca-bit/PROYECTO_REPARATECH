package com.cibertec.cibertecapp.features.requests.domain.repository

import android.net.Uri
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest

interface RequestRepository {
    suspend fun getMyRequests(): List<QuotationRequest>
    suspend fun createRequest(request: QuotationRequest, imageUri: Uri?): Result<Unit>
    suspend fun deleteRequest(requestId: String): Result<Unit>
}
