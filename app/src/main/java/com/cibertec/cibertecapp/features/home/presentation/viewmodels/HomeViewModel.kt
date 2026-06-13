package com.cibertec.cibertecapp.features.home.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.cibertec.cibertecapp.features.home.data.repository.HomeRepositoryImpl
import com.cibertec.cibertecapp.features.home.domain.usecases.FilterRepairsByCategoryUseCase
import com.cibertec.cibertecapp.features.home.domain.usecases.GetRepairsUseCase
import com.cibertec.cibertecapp.features.home.domain.usecases.SearchRepairsUseCase
import com.cibertec.cibertecapp.features.home.presentation.state.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepositoryImpl(application)
    private val getRepairsUseCase = GetRepairsUseCase(repository)
    private val filterRepairsByCategoryUseCase = FilterRepairsByCategoryUseCase(repository)
    private val searchRepairsUseCase = SearchRepairsUseCase(repository)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private var allRepairs = emptyList<Repair>()
    private var currentCategory = ""
    private var currentQuery = ""

    init {
        loadRepairs()
    }

    fun loadRepairs() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            allRepairs = getRepairsUseCase()
            
            _state.value = _state.value.copy(
                isLoading = false,
                userName = profile.name,
                userPhotoUrl = profile.photoUrl
            )
            applyFilters()
        }
    }

    fun filterRepairs(category: String) {
        currentCategory = if (currentCategory == category) "" else category
        _selectedCategory.value = currentCategory
        applyFilters()
    }

    fun searchRepairs(query: String) {
        currentQuery = query
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
        _state.value = _state.value.copy(
            repairs = filteredList.take(3)
        )
    }
}
