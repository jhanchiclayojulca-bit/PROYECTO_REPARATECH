package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.cibertec.cibertecapp.databinding.ActivityDeviceDetailBinding

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = intent.getStringExtra("DEVICE_ID")
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val device = ServiceRepository.deviceList.find { it.id == deviceId }
        
        if (device != null) {
            binding.deviceName.text = device.name
            binding.tvBrand.text = device.name.split(" ").firstOrNull() ?: ""
            binding.tvModel.text = device.name.substringAfter(" ", "")
            binding.tvSerial.text = device.serial
            binding.statusChip.text = device.status

            if (device.status == "Garantía Expirada") {
                binding.statusChip.setChipBackgroundColorResource(R.color.error_container)
                binding.statusChip.setTextColor(getColor(R.color.on_error_container))
            } else {
                binding.statusChip.setChipBackgroundColorResource(R.color.success_container)
                binding.statusChip.setTextColor(getColor(R.color.on_success_container))
            }

            // Imagen dinámica según categoría
            val imageUrl = when (device.category) {
                "Laptops" -> "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&q=80&w=1000"
                "Celulares" -> "https://images.unsplash.com/photo-1616348436168-de43ad0db179?auto=format&fit=crop&q=80&w=1000"
                "Gaming" -> "https://images.unsplash.com/photo-1486401899868-0e435ed85128?auto=format&fit=crop&q=80&w=1000"
                else -> "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=1000"
            }

            binding.deviceImage.load(imageUrl) {
                crossfade(true)
                placeholder(device.iconRes)
                error(device.iconRes)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cardDelete.setOnClickListener {
            ServiceRepository.deviceList.removeAll { it.id == deviceId }
            ServiceRepository.save(this)
            Toast.makeText(this, "Dispositivo eliminado", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.cardEdit.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            intent.putExtra("DEVICE_ID", deviceId)
            startActivity(intent)
            finish()
        }

        binding.btnSupport.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            val device = ServiceRepository.deviceList.find { it.id == deviceId }
            intent.putExtra("DEVICE_NAME", device?.name)
            intent.putExtra("DEVICE_CATEGORY", device?.category)
            startActivity(intent)
        }
    }
}
