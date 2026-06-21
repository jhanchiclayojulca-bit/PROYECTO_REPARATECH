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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepositoryImpl(application)
    private val getRepairsUseCase = GetRepairsUseCase(repository)
    private val filterRepairsByCategoryUseCase = FilterRepairsByCategoryUseCase(repository)
    private val searchRepairsUseCase = SearchRepairsUseCase(repository)
    
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private var allRepairs = emptyList<Repair>()
    private var currentCategory = ""
    private var currentQuery = ""

    init {
        loadRepairs()
        listenToProfileChanges()
        listenToRepairs() // ESCUCHA EN TIEMPO REAL PARA REPARACIONES
    }

    private fun listenToRepairs() {
        val user = auth.currentUser ?: return
        db.collection("repairs")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allRepairs = snapshot.documents.mapNotNull { doc ->
                        // Mapeo manual similar al del repositorio para mantener consistencia
                        val status = doc.getString("status") ?: "PENDIENTE"
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        Repair(
                            id = doc.id,
                            deviceId = doc.getString("deviceId") ?: "",
                            deviceName = doc.getString("brandAndModel") ?: "Desconocido",
                            orderId = doc.getString("orderId") ?: "N/A",
                            status = status,
                            progress = when(status.uppercase()) {
                                "COMPLETADO" -> 100
                                "PROGRESO" -> 50
                                "REVISIÓN" -> 25
                                else -> 10
                            },
                            date = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date(createdAt)),
                            photoUrl = doc.getString("photoUrl") ?: "",
                            category = doc.getString("deviceCategory") ?: "",
                            service = doc.getString("problemDescription") ?: "Sin descripción",
                            deliveryMethod = doc.getString("deliveryMethod") ?: "Presencial",
                            technician = "Técnico ReparaTech",
                            baseCost = doc.getDouble("baseCost") ?: 0.0,
                            tax = doc.getDouble("tax") ?: 0.0,
                            additionalCost = doc.getDouble("additionalCost") ?: 0.0,
                            total = doc.getDouble("total") ?: 0.0,
                            createdAt = createdAt
                        )
                    }.sortedByDescending { it.createdAt }
                    applyFilters()
                }
            }
    }

    private fun listenToProfileChanges() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("name") ?: user.displayName ?: "Usuario"
                    val phone = snapshot.getString("phone") ?: ""
                    val photo = snapshot.getString("photoUrl") ?: user.photoUrl?.toString() ?: ""
                    
                    _state.update { 
                        it.copy(
                            userName = name,
                            userPhone = phone,
                            userPhotoUrl = photo
                        )
                    }
                }
            }
    }

    fun loadRepairs() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            allRepairs = getRepairsUseCase()
            
            _state.value = _state.value.copy(
                isLoading = false,
                userName = profile.name,
                userPhotoUrl = profile.photoUrl,
                userPhone = profile.phone
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
