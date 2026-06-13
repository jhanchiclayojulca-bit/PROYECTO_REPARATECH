package com.cibertec.cibertecapp.features.home.domain.usecases

import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.domain.repository.HomeRepository

class FilterRepairsByCategoryUseCase(
    private val repository: HomeRepository
) {

    operator fun invoke(
        repairs: List<Repair>,
        category: String
    ): List<Repair> {

        return repository.filterRepairsByCategory(
            repairs,
            category
        )
    }
}