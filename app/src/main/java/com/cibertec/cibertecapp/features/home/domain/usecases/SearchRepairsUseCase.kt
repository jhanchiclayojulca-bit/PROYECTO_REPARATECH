package com.cibertec.cibertecapp.features.home.domain.usecases


import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.domain.repository.HomeRepository

class SearchRepairsUseCase(
    private val repository: HomeRepository
) {

    operator fun invoke(
        repairs: List<Repair>,
        query: String
    ): List<Repair> {

        return repository.searchRepairs(
            repairs,
            query
        )
    }
}