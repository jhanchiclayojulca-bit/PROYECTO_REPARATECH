package com.cibertec.cibertecapp.features.requests.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.requests.data.repository.RequestRepositoryImpl
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import com.cibertec.cibertecapp.features.requests.presentation.state.RequestsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class RequestsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RequestRepositoryImpl(application)
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(RequestsState())
    val state: StateFlow<RequestsState> = _state.asStateFlow()

    init {
        loadOfflineFirst()
        listenToRequests()
    }

    private fun loadOfflineFirst() {
        viewModelScope.launch {
            val offline = repository.getOfflineRequests()
            if (offline.isNotEmpty()) {
                _state.update { it.copy(requests = offline) }
            }
        }
    }

    private fun listenToRequests() {
        val user = auth.currentUser ?: return
        db.collection("requests")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(QuotationRequest::class.java)?.apply { id = doc.id }
                    }.sortedByDescending { it.createdAt }
                    
                    _state.update { it.copy(requests = remoteList) }
                    
                    // Sincronizar con Room
                    viewModelScope.launch {
                        repository.saveRequestsToLocal(remoteList)
                    }
                }
            }
    }

    fun loadRequests() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val list = repository.getMyRequests()
            _state.update { it.copy(isLoading = false, requests = list) }
        }
    }

    fun deleteRequest(id: String) {
        viewModelScope.launch {
            repository.deleteRequest(id)
            loadRequests()
        }
    }
}
