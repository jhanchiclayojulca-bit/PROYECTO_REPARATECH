package com.cibertec.cibertecapp.features.support.presentation.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivitySupportBinding
import com.cibertec.cibertecapp.features.support.presentation.viewmodels.SupportViewModel
import kotlinx.coroutines.launch

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding
    private val viewModel: SupportViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPreview.load(it)
            binding.ivPreview.visibility = View.VISIBLE
            binding.layoutEmptyPhoto.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDropdown()
        setupListeners()
        observeState()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdown() {
        val problems = arrayOf("Falla de Hardware", "Problema de Software", "Pantalla / Display", "Batería / Carga", "Mantenimiento Preventivo", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, problems)
        binding.atvProblemType.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnPickPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            val type = binding.atvProblemType.text.toString()
            val desc = binding.etDescription.text.toString()
            val device = intent.getStringExtra("DEVICE_NAME") ?: ""

            if (type.isBlank() || desc.isBlank()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.sendSupportRequest(device, "Otros", type, desc, selectedImageUri)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.btnSubmit.isEnabled = !loading
                binding.btnSubmit.text = if (loading) "Enviando..." else "Enviar a Soporte"
            }
        }

        lifecycleScope.launch {
            viewModel.isSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@SupportActivity, "Consulta enviada. Revisa 'Mis Solicitudes'", Toast.LENGTH_LONG).show()
                    viewModel.resetSuccess()
                    finish()
                }
            }
        }
    }
}
