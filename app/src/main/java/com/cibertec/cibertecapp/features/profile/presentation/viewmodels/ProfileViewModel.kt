package com.cibertec.cibertecapp.features.profile.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.cibertecapp.features.profile.data.repository.ProfileRepositoryImpl
import com.cibertec.cibertecapp.features.profile.domain.usecases.GetProfileUseCase
import com.cibertec.cibertecapp.features.profile.presentation.state.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepositoryImpl()
    private val getProfileUseCase = GetProfileUseCase(repository)
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        listenToProfileChanges() // ESCUCHA EN TIEMPO REAL
    }

    private fun listenToProfileChanges() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _state.update {
                        it.copy(
                            name = snapshot.getString("name") ?: it.name,
                            phone = snapshot.getString("phone") ?: it.phone,
                            address = snapshot.getString("address") ?: it.address,
                            avatarUrl = snapshot.getString("photoUrl") ?: it.avatarUrl
                        )
                    }
                }
            }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val profile = getProfileUseCase()
                val repairCount = repository.getRepairCount()
                val deviceCount = repository.getDeviceCount()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        name = profile.name,
                        email = profile.email,
                        phone = profile.phone,
                        address = profile.address,
                        avatarUrl = profile.avatarUrl,
                        totalRepairs = repairCount,
                        totalDevices = deviceCount
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun logout() {
        repository.logout()
        _state.update { it.copy(isLoggedOut = true) }
    }
}
