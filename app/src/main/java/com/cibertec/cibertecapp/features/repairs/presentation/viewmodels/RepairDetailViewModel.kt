package com.cibertec.cibertecapp.features.repairs.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RepairDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _repair = MutableStateFlow<Repair?>(null)
    val repair: StateFlow<Repair?> = _repair

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRepairDetails(repairId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val doc = db.collection("repairs").document(repairId).get().await()
                if (doc.exists()) {
                    val status = doc.getString("status") ?: "PENDIENTE"
                    _repair.value = Repair(
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
                        date = "Fecha no disponible",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        category = doc.getString("deviceCategory") ?: "",
                        service = doc.getString("problemDescription") ?: "Sin descripción",
                        deliveryMethod = doc.getString("deliveryMethod") ?: "Presencial",
                        technician = "Técnico Asignado",
                        baseCost = doc.getDouble("baseCost") ?: 0.0,
                        tax = doc.getDouble("tax") ?: 0.0,
                        additionalCost = doc.getDouble("additionalCost") ?: 0.0,
                        total = doc.getDouble("total") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("RepairDetail", "Error loading: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
