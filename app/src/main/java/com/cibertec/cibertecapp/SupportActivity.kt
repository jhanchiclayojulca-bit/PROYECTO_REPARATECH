package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProblemTypeDropdown()
        setupListeners()
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

        val deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Equipo Registrado"
        val category = intent.getStringExtra("DEVICE_CATEGORY") ?: "General"

        binding.btnSubmit.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            val problemType = binding.atvProblemType.text.toString()
            
            if (description.isEmpty()) {
                Toast.makeText(this, "Por favor, describe brevemente el problema", Toast.LENGTH_SHORT).show()
            } else {
                // Crear nueva solicitud con persistencia real
                val newRequest = ServiceRequest(
                    deviceName = deviceName, 
                    category = category,
                    problemType = problemType,
                    description = description,
                    iconRes = when(category) {
                        "Laptops" -> R.drawable.ic_laptop
                        "Celulares" -> R.drawable.ic_smartphone
                        "Gaming" -> R.drawable.ic_gaming
                        else -> R.drawable.ic_devices
                    },
                    status = "PENDIENTE DE PAGO",
                    price = 100.00, // Precio fijo solicitado de 100 soles
                    isPaid = false
                )
                ServiceRepository.serviceList.add(0, newRequest)
                ServiceRepository.save(this)

                // Ir directamente a Solicitudes como se solicitó
                val intent = Intent(this, RequestsClientActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }
}
