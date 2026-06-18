package com.cibertec.cibertecapp.features.support.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivitySupportBinding
import com.cibertec.cibertecapp.features.support.presentation.viewmodels.SupportViewModel
import com.cibertec.cibertecapp.features.requests.presentation.activities.RequestsActivity
import kotlinx.coroutines.launch

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding
    private val viewModel: SupportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProblemTypeDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupProblemTypeDropdown() {
        val problems = arrayOf(
            "Falla de Hardware (Pantalla, Batería, etc.)",
            "Error de Software / Sistema Operativo",
            "Mantenimiento Preventivo / Limpieza",
            "Otros Problemas Técnicos"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, problems)
        binding.atvProblemType.setAdapter(adapter)
        binding.atvProblemType.setText(problems[0], false)
    }

    private fun setupListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Equipo General"
        val category = intent.getStringExtra("DEVICE_CATEGORY") ?: "Varios"

        binding.btnSubmit.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            val problemType = binding.atvProblemType.text.toString()
            
            if (description.isEmpty()) {
                Toast.makeText(this, "Describe el problema", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sendSupportRequest(deviceName, category, problemType, description)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.btnSubmit.isEnabled = !isLoading
                binding.btnSubmit.text = if (isLoading) "Enviando..." else "Enviar a Soporte"
            }
        }

        lifecycleScope.launch {
            viewModel.isSuccess.collect { success ->
                if (success) {
                    Toast.makeText(this@SupportActivity, "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SupportActivity, RequestsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    viewModel.resetSuccess()
                    finish()
                }
            }
        }
    }
}
