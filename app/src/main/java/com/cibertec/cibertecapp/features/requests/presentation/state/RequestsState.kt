package com.cibertec.cibertecapp.features.requests.presentation.state

import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest

data class RequestsState(
    val requests: List<QuotationRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)