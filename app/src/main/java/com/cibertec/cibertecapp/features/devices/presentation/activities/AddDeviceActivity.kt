package com.cibertec.cibertecapp.features.devices.presentation.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityAddDeviceModernBinding
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.presentation.viewmodels.DevicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDeviceModernBinding
    private val viewModel: DevicesViewModel by viewModels()
    private var selectedCategory = ""
    private var isEditMode = false
    private var existingDevice: Device? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateImageUri(it)
            binding.ivPreview.load(it)
            binding.ivPreview.visibility = View.VISIBLE
            binding.layoutEmptyPhoto.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceModernBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        existingDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("DEVICE_DATA", Device::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("DEVICE_DATA")
        }

        setupToolbar()
        setupCategories()
        setupListeners()
        observeState()

        if (isEditMode && existingDevice != null) {
            loadExistingData(existingDevice!!)
        }
    }

    private fun loadExistingData(device: Device) {
        binding.tvTitle.text = "Editar Equipo"
        binding.etBrand.setText(device.brand)
        binding.etModel.setText(device.model)
        binding.etSerial.setText(device.serialNumber)
        selectCategory(device.category)
        
        if (device.photoUrl.isNotEmpty()) {
            binding.ivPreview.load(device.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_laptop)
            }
            binding.ivPreview.visibility = View.VISIBLE
            binding.layoutEmptyPhoto.visibility = View.GONE
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupCategories() {
        binding.cardSmartphone.tvCatName.text = "Smartphone"
        binding.cardSmartphone.ivCatIcon.setImageResource(R.drawable.ic_smartphone)
        
        binding.cardLaptop.tvCatName.text = "Laptop"
        binding.cardLaptop.ivCatIcon.setImageResource(R.drawable.ic_laptop)
        
        binding.cardTablet.tvCatName.text = "Tablet"
        binding.cardTablet.ivCatIcon.setImageResource(R.drawable.ic_devices)

        binding.cardGaming.tvCatName.text = "Gaming"
        binding.cardGaming.ivCatIcon.setImageResource(R.drawable.ic_gaming)

        binding.cardSmartphone.root.setOnClickListener { selectCategory("Smartphone") }
        binding.cardLaptop.root.setOnClickListener { selectCategory("Laptop") }
        binding.cardTablet.root.setOnClickListener { selectCategory("Tablet") }
        binding.cardGaming.root.setOnClickListener { selectCategory("Gaming") }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        
        val cards = listOf(
            "Smartphone" to binding.cardSmartphone,
            "Laptop" to binding.cardLaptop,
            "Tablet" to binding.cardTablet,
            "Gaming" to binding.cardGaming
        )
        
        cards.forEach { (cat, chip) ->
            val isSelected = (cat == category)
            chip.root.strokeWidth = if (isSelected) 6 else 2
            chip.root.setStrokeColor(getColor(if (isSelected) R.color.brand_blue else R.color.outline_variant))
            chip.tvCatName.setTextColor(getColor(if (isSelected) R.color.brand_blue else R.color.on_surface_variant))
            chip.ivCatIcon.setColorFilter(getColor(if (isSelected) R.color.brand_blue else R.color.on_surface_variant))
        }
    }

    private fun setupListeners() {
        binding.cardUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnSaveDevice.setOnClickListener {
            val brand = binding.etBrand.text.toString()
            val model = binding.etModel.text.toString()
            val serial = binding.etSerial.text.toString()
            viewModel.saveDevice(brand, model, serial, selectedCategory, existingDevice)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnSaveDevice.isEnabled = !state.isLoading

                if (state.isSuccess) {
                    binding.tvTitle.text = "¡Todo listo!"
                    binding.btnSaveDevice.text = "Sincronizando..."
                    
                    delay(1500) // Pausa de seguridad
                    
                    val message = if (isEditMode) "Equipo actualizado" else "Equipo registrado con éxito"
                    Toast.makeText(this@AddDeviceActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.resetSuccess()
                    finish()
                }
                state.error?.let {
                    Toast.makeText(this@AddDeviceActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
