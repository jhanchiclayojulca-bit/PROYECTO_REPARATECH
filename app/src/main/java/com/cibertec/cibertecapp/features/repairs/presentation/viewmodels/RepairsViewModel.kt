package com.cibertec.cibertecapp.features.repairs.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.home.data.repository.HomeRepositoryImpl
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.domain.usecases.FilterRepairsByCategoryUseCase
import com.cibertec.cibertecapp.features.home.domain.usecases.GetRepairsUseCase
import com.cibertec.cibertecapp.features.home.domain.usecases.SearchRepairsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RepairsState(
    val repairs: List<Repair> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RepairsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepositoryImpl(application)
    private val getRepairsUseCase = GetRepairsUseCase(repository)
    private val filterRepairsByCategoryUseCase = FilterRepairsByCategoryUseCase(repository)
    private val searchRepairsUseCase = SearchRepairsUseCase(repository)

    private val _state = MutableStateFlow(RepairsState())
    val state: StateFlow<RepairsState> = _state.asStateFlow()

    private var allRepairs = emptyList<Repair>()
    private var currentCategory = ""
    private var currentQuery = ""

    fun loadRepairs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            allRepairs = getRepairsUseCase()
            _state.update { it.copy(isLoading = false) }
            applyFilters()
        }
    }

    fun searchRepairs(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun filterByCategory(category: String) {
        currentCategory = if (currentCategory == category) "" else category
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = allRepairs
        if (currentCategory.isNotEmpty()) {
            filteredList = filterRepairsByCategoryUseCase(filteredList, currentCategory)
        }
        if (currentQuery.isNotEmpty()) {
            filteredList = searchRepairsUseCase(filteredList, currentQuery)
        }
        _state.update { it.copy(repairs = filteredList) }
    }
}
