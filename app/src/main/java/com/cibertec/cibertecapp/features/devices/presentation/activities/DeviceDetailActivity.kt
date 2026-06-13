package com.cibertec.cibertecapp.features.devices.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityDeviceDetailBinding
import com.cibertec.cibertecapp.features.devices.presentation.viewmodels.DeviceDetailViewModel
import com.cibertec.cibertecapp.features.home.presentation.adapters.RepairAdapter
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairDetailActivity
import com.cibertec.cibertecapp.features.support.presentation.activities.SupportActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private val viewModel: DeviceDetailViewModel by viewModels()
    private var deviceId: String? = null
    private lateinit var repairAdapter: RepairAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent.getStringExtra("DEVICE_ID")

        setupToolbar()
        setupRecycler()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        deviceId?.let { viewModel.loadDeviceDetails(it) }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        repairAdapter = RepairAdapter { repair ->
            val intent = Intent(this, RepairDetailActivity::class.java).apply {
                putExtra("REPAIR_ID", repair.id)
            }
            startActivity(intent)
        }
        binding.rvRepairHistory.apply {
            adapter = repairAdapter
            layoutManager = LinearLayoutManager(this@DeviceDetailActivity)
        }
    }

    private fun setupListeners() {
        binding.cardDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.cardEdit.setOnClickListener {
            val currentDevice = viewModel.device.value
            if (currentDevice != null) {
                val intent = Intent(this, AddDeviceActivity::class.java).apply {
                    putExtra("EDIT_MODE", true)
                    putExtra("DEVICE_DATA", currentDevice)
                }
                startActivity(intent)
            }
        }

        binding.btnSupport.setOnClickListener {
            val currentDevice = viewModel.device.value
            val intent = Intent(this, SupportActivity::class.java).apply {
                putExtra("DEVICE_NAME", "${currentDevice?.brand} ${currentDevice?.model}")
                putExtra("DEVICE_CATEGORY", currentDevice?.category)
            }
            startActivity(intent)
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar equipo")
            .setMessage("¿Estás seguro de que quieres eliminar este equipo? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteDevice()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.device.collect { device ->
                device?.let {
                    binding.deviceName.text = "${it.brand} ${it.model}"
                    binding.tvBrand.text = it.brand
                    binding.tvModel.text = it.model
                    binding.tvSerial.text = it.serialNumber
                    binding.statusChip.text = it.status.uppercase()

                    // Cambiar color de fondo del badge según estado
                    val badgeColor = if (it.status.equals("Activo", true)) "#E8F1FF" else "#FFF0F0"
                    val textColor = if (it.status.equals("Activo", true)) R.color.brand_blue else R.color.status_orange_text
                    
                    binding.statusChip.background.setTint(android.graphics.Color.parseColor(badgeColor))
                    binding.statusChip.setTextColor(getColor(textColor))

                    // Imagen dinámica con manejo de estados de carga
                    if (it.photoUrl.isNotEmpty()) {
                        binding.imageShimmer.visibility = View.VISIBLE
                        binding.imageShimmer.startShimmer()
                        
                        binding.deviceImage.load(it.photoUrl) {
                            crossfade(true)
                            crossfade(400)
                            listener(
                                onSuccess = { _, _ ->
                                    binding.imageShimmer.stopShimmer()
                                    binding.imageShimmer.visibility = View.GONE
                                },
                                onError = { _, _ ->
                                    binding.imageShimmer.stopShimmer()
                                    binding.imageShimmer.visibility = View.GONE
                                    binding.deviceImage.setImageResource(R.drawable.ic_laptop)
                                }
                            )
                        }
                    } else {
                        binding.imageShimmer.visibility = View.GONE
                        binding.deviceImage.setImageResource(R.drawable.ic_laptop)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.repairs.collect { repairs ->
                if (repairs.isNotEmpty()) {
                    binding.tvHistoryTitle.visibility = View.VISIBLE
                    binding.rvRepairHistory.visibility = View.VISIBLE
                    repairAdapter.updateList(repairs)
                } else {
                    binding.tvHistoryTitle.visibility = View.GONE
                    binding.rvRepairHistory.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isDeleted.collect { isDeleted ->
                if (isDeleted) {
                    Toast.makeText(this@DeviceDetailActivity, "Equipo eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
