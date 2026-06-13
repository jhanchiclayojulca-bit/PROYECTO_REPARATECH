package com.cibertec.cibertecapp.features.devices.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.home.domain.model.Repair
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device.asStateFlow()

    private val _repairs = MutableStateFlow<List<Repair>>(emptyList())
    val repairs: StateFlow<List<Repair>> = _repairs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted

    fun loadDeviceDetails(deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Cargar datos del equipo
                val doc = db.collection("devices").document(deviceId).get().await()
                if (doc.exists()) {
                    _device.value = doc.toObject(Device::class.java)?.apply { id = doc.id }
                }

                // 2. Cargar historial de reparaciones vinculadas a este equipo
                val repairsSnapshot = db.collection("repairs")
                    .whereEqualTo("deviceId", deviceId)
                    .get()
                    .await()

                val repairList = repairsSnapshot.documents.map { rDoc ->
                    val createdAt = rDoc.getLong("createdAt") ?: 0L
                    val dateFormatted = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(createdAt))
                    
                    Repair(
                        id = rDoc.id,
                        deviceId = deviceId,
                        deviceName = rDoc.getString("brandAndModel") ?: "Desconocido",
                        orderId = rDoc.getString("orderId") ?: "N/A",
                        status = rDoc.getString("status") ?: "PENDIENTE",
                        progress = when(rDoc.getString("status")?.uppercase()) {
                            "COMPLETADO" -> 100
                            "PROGRESO" -> 50
                            "REVISIÓN" -> 25
                            else -> 10
                        },
                        date = dateFormatted,
                        photoUrl = rDoc.getString("photoUrl") ?: "",
                        category = rDoc.getString("deviceCategory") ?: "",
                        service = rDoc.getString("problemDescription") ?: "Sin descripción",
                        deliveryMethod = rDoc.getString("deliveryMethod") ?: "Presencial",
                        technician = "Técnico ReparaTech",
                        baseCost = rDoc.getDouble("baseCost") ?: 0.0,
                        additionalCost = rDoc.getDouble("additionalCost") ?: 0.0,
                        total = rDoc.getDouble("total") ?: 0.0,
                        createdAt = createdAt
                    )
                }.sortedByDescending { it.createdAt }

                _repairs.value = repairList

            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDevice() {
        val currentDevice = _device.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("devices").document(currentDevice.id).delete().await()
                _isDeleted.value = true
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
