package com.cibertec.cibertecapp.features.home.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.cibertec.cibertecapp.features.home.data.model.RepairModel
import com.cibertec.cibertecapp.features.home.data.repository.HomeRepositoryImpl
import com.cibertec.cibertecapp.features.home.domain.usecases.GetRepairsUseCase
import com.cibertec.cibertecapp.features.home.presentation.state.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val repository = HomeRepositoryImpl()
    private val getRepairsUseCase = GetRepairsUseCase(repository)
    private val _state = MutableStateFlow(HomeState())

    private var allRepairs = emptyList<RepairModel>()

    val state: StateFlow<HomeState> = _state

    init {
        loadRepairs()
    }

    private fun loadRepairs() {

        allRepairs = getRepairsUseCase()

        _state.value = _state.value.copy(
            repairs = allRepairs
        )
    }

    fun filterRepairs(category: String) {

        val filteredList = allRepairs.filter {

            it.category == category
        }

        _state.value = _state.value.copy(
            repairs = filteredList
        )
    }

    fun showAllRepairs() {

        _state.value = _state.value.copy(
            repairs = allRepairs
        )
    }

}