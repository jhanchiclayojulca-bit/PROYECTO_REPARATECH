package com.cibertec.cibertecapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityPersonalInfoBinding

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun enterEditMode() {
        isEditMode = true
        binding.displayContainer.visibility = View.GONE
        binding.editContainer.visibility = View.VISIBLE
        
        // Cargar valores actuales en los campos
        binding.etName.setText(binding.tvDisplayName.text)
        binding.etEmail.setText(binding.tvDisplayEmail.text)
        binding.etPhone.setText(binding.tvDisplayPhone.text)
        binding.etAddress.setText(binding.tvDisplayAddress.text)

        binding.btnEditInfo.text = "Guardar Cambios"
        binding.btnEditInfo.setIconResource(R.drawable.ic_check)
    }

    private fun saveChanges() {
        val newName = binding.etName.text.toString()
        val newEmail = binding.etEmail.text.toString()
        val newPhone = binding.etPhone.text.toString()
        val newAddress = binding.etAddress.text.toString()

        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Aplicar cambios a la vista de visualización
        binding.tvDisplayName.text = newName
        binding.tvDisplayEmail.text = newEmail
        binding.tvDisplayPhone.text = newPhone
        binding.tvDisplayAddress.text = newAddress

        isEditMode = false
        binding.displayContainer.visibility = View.VISIBLE
        binding.editContainer.visibility = View.GONE
        
        binding.btnEditInfo.text = "Editar Información"
        binding.btnEditInfo.setIconResource(R.drawable.ic_edit)
        
        Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
    }
}