package com.cibertec.cibertecapp.features.home.domain.usecases

import com.cibertec.cibertecapp.features.home.domain.repository.HomeRepository

class GetRepairsUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke() = repository.getRepairs()
}