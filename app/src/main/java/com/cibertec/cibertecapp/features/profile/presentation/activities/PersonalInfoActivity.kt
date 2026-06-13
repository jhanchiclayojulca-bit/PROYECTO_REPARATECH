package com.cibertec.cibertecapp.features.profile.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityPersonalInfoBinding
import com.cibertec.cibertecapp.features.profile.presentation.viewmodels.EditProfileViewModel
import kotlinx.coroutines.launch

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEditInfo.setOnClickListener {
            if (!isEditMode) {
                enterEditMode()
            } else {
                saveChanges()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { profile ->
                profile?.let {
                    binding.tvDisplayName.text = it.name
                    binding.tvDisplayEmail.text = it.email
                    binding.tvDisplayPhone.text = it.phone
                    binding.tvDisplayAddress.text = it.address
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@PersonalInfoActivity, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                    exitEditMode()
                }
            }
        }
    }

    private fun enterEditMode() {
        isEditMode = true
        binding.displayContainer.visibility = View.GONE
        binding.editContainer.visibility = View.VISIBLE
        
        binding.etName.setText(binding.tvDisplayName.text)
        binding.etEmail.setText(binding.tvDisplayEmail.text)
        binding.etPhone.setText(binding.tvDisplayPhone.text)
        binding.etAddress.setText(binding.tvDisplayAddress.text)
        
        binding.etEmail.isEnabled = false // El correo no se suele cambiar así

        binding.btnEditInfo.text = "Guardar Cambios"
        binding.btnEditInfo.setIconResource(R.drawable.ic_check)
    }

    private fun exitEditMode() {
        isEditMode = false
        binding.displayContainer.visibility = View.VISIBLE
        binding.editContainer.visibility = View.GONE
        binding.btnEditInfo.text = "Editar Información"
        binding.btnEditInfo.setIconResource(R.drawable.ic_edit)
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()
        val address = binding.etAddress.text.toString()

        if (name.isBlank() || phone.isBlank() || address.isBlank()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfile(name, phone, address)
    }
}
