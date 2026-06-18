package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityRepairDetailBinding
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.RepairDetailViewModel
import com.cibertec.cibertecapp.features.support.presentation.activities.SupportActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

class RepairDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRepairDetailBinding
    private val viewModel: RepairDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepairDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repairId = intent.getStringExtra("REPAIR_ID")
        repairId?.let { viewModel.loadRepairDetails(it) }

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnSupportChat.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java).apply {
                putExtra("DEVICE_NAME", binding.tvDeviceName.text.toString())
            }
            startActivity(intent)
        }

        binding.btnCancelRepair.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun showCancelConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Cancelar reparación?")
            .setMessage("Esta acción es irreversible y eliminará el registro de reparación.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                viewModel.cancelRepair()
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.repair.collect { repair ->
                repair?.let {
                    binding.tvDeviceName.text = it.deviceName
                    binding.tvOrderId.text = it.orderId
                    binding.tvStatus.text = it.status.uppercase()
                    binding.tvDescription.text = it.service

                    val parts = it.deviceName.split(" ", limit = 2)
                    binding.tvBrand.text = parts.getOrNull(0) ?: "-"
                    binding.tvModel.text = parts.getOrNull(1) ?: "-"
                    
                    binding.tvLogistics.text = if (it.deliveryMethod == "Recogida") 
                        "Recojo a domicilio programado" 
                    else 
                        "Entrega en tienda física"
                    
                    binding.repairProgress.progress = it.progress
                    binding.tvProgressLabel.text = "Progreso de reparación: ${it.progress}%"

                    binding.tvBaseCost.text = String.format(Locale.getDefault(), "S/.%.2f", it.baseCost)
                    binding.tvTax.text = String.format(Locale.getDefault(), "S/.%.2f", it.tax)
                    binding.tvAdditionalCost.text = String.format(Locale.getDefault(), "S/.%.2f", it.additionalCost)
                    binding.tvTotal.text = String.format(Locale.getDefault(), "S/.%.2f", it.total)

                    binding.ivDeviceImage.load(it.photoUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_laptop)
                        error(R.drawable.ic_laptop)
                    }

                    // Mostrar botón de cancelar SOLO si el estado es PENDIENTE
                    binding.btnCancelRepair.visibility = if (it.status.uppercase() == "PENDIENTE") 
                        View.VISIBLE else View.GONE

                    when(it.status.uppercase()) {
                        "COMPLETADO" -> {
                            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_green_bg))
                            binding.tvStatus.setTextColor(getColor(R.color.status_green_text))
                        }
                        "PROGRESO" -> {
                            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_blue_bg))
                            binding.tvStatus.setTextColor(getColor(R.color.status_blue_text))
                        }
                        "REVISIÓN", "REVISION" -> {
                            binding.cardStatus.setCardBackgroundColor(getColor(R.color.status_orange_bg))
                            binding.tvStatus.setTextColor(getColor(R.color.status_orange_text))
                        }
                        else -> {
                            binding.cardStatus.setCardBackgroundColor(getColor(R.color.brand_blue_light))
                            binding.tvStatus.setTextColor(getColor(R.color.brand_blue))
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isDeleted.collect { isDeleted ->
                if (isDeleted) {
                    Toast.makeText(this@RepairDetailActivity, "Reparación cancelada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
