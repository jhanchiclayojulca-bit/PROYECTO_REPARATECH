package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getStringExtra("ORDER_ID")
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val request = ServiceRepository.serviceList.find { it.id == orderId }
        request?.let {
            binding.tvPaymentDevice.text = it.deviceName
            binding.tvPaymentAmount.text = "S/ ${String.format("%.2f", it.price)}"
        }
    }

    private fun setupListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnConfirmPayment.setOnClickListener {
            // Actualizar estado en el repositorio
            val index = ServiceRepository.serviceList.indexOfFirst { it.id == orderId }
            if (index != -1) {
                val old = ServiceRepository.serviceList[index]
                ServiceRepository.serviceList[index] = old.copy(
                    status = "EN PROCESO",
                    isPaid = true,
                    progress = 10,
                    technicianName = "Ing. Ricardo Palma" // Asignar técnico al pagar
                )
                ServiceRepository.save(this)
                
                // Mostrar mensaje moderno personalizado
                val dialogView = layoutInflater.inflate(R.layout.dialog_payment_detail, null)
                dialogView.findViewById<android.widget.TextView>(R.id.tvDialogOrderId).text = "#${old.id}"
                dialogView.findViewById<android.widget.TextView>(R.id.tvDialogDevice).text = old.deviceName
                dialogView.findViewById<android.widget.TextView>(R.id.tvDialogAmount).text = "S/ ${String.format("%.2f", old.price)}"

                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setView(dialogView)
                    .setPositiveButton("Regresar") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
}
