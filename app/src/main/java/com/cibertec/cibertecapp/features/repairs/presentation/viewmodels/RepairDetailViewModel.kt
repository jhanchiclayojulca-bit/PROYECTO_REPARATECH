package com.cibertec.cibertecapp.features.repairs.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.home.data.repository.HomeRepositoryImpl
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RepairDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepositoryImpl(application)
    private val db = FirebaseFirestore.getInstance()
    private val _repair = MutableStateFlow<Repair?>(null)
    val repair: StateFlow<Repair?> = _repair

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted

    fun loadRepairDetails(repairId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Carga "instantánea" desde el repositorio (Room -> Firebase)
            val result = repository.getRepairById(repairId)
            if (result != null) {
                _repair.value = result
            }
            _isLoading.value = false
        }
    }

    fun cancelRepair() {
        val repairId = _repair.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("repairs").document(repairId).delete().await()
                _isDeleted.value = true
            } catch (e: Exception) {
                android.util.Log.e("RepairDetail", "Error deleting: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveRating(rating: Float, comment: String) {
        val repairId = _repair.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Usamos el repositorio para guardar la calificación
                repository.saveRepairRating(repairId, rating, comment)
                
                // Actualizar estado local
                _repair.value = _repair.value?.copy(
                    rating = rating,
                    ratingComment = comment
                )
            } catch (e: Exception) {
                android.util.Log.e("RepairDetail", "Error saving rating: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
