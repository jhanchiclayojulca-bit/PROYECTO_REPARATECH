package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.databinding.ActivityDevicesClientBinding

class DevicesClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupTopBar()
        setupBottomNavigation()
        setupClickListeners()
        setupChipFilters()
        refreshDeviceList()
    }

    override fun onResume() {
        super.onResume()
        refreshDeviceList()
    }

    private fun refreshDeviceList() {
        binding.devicesContainer.removeAllViews()
        ServiceRepository.deviceList.forEach { device ->
            addDeviceToUI(device)
        }
    }

    private fun addDeviceToUI(device: UserDevice) {
        val deviceView = layoutInflater.inflate(R.layout.item_device_card, binding.devicesContainer, false)
        
        val name = deviceView.findViewById<TextView>(R.id.tvDeviceName)
        val serial = deviceView.findViewById<TextView>(R.id.tvDeviceSerial)
        val status = deviceView.findViewById<TextView>(R.id.tvDeviceStatus)
        val icon = deviceView.findViewById<ImageView>(R.id.ivDeviceIcon)
        val btnDetails = deviceView.findViewById<View>(R.id.btnDetails)
        val btnSupport = deviceView.findViewById<View>(R.id.btnSupport)

        name.text = device.name
        serial.text = "S/N: ${device.serial}"
        status.text = device.status
        icon.setImageResource(device.iconRes)

        btnDetails.setOnClickListener {
            val intent = Intent(this, DeviceDetailActivity::class.java)
            intent.putExtra("DEVICE_ID", device.id)
            startActivity(intent)
        }

        btnSupport.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            intent.putExtra("DEVICE_NAME", device.name)
            intent.putExtra("DEVICE_CATEGORY", device.category)
            startActivity(intent)
        }

        binding.devicesContainer.addView(deviceView)
    }

    private fun setupChipFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipLaptops -> "Laptops"
                R.id.chipCelulares -> "Celulares"
                R.id.chipGaming -> "Gaming"
                R.id.chipTablets -> "Tablets"
                else -> "Todos"
            }
            
            binding.devicesContainer.removeAllViews()
            ServiceRepository.deviceList.filter { it.category == filter || filter == "Todos" }.forEach { 
                addDeviceToUI(it)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "No tienes notificaciones nuevas", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddDevice.setOnClickListener {
            startActivity(Intent(this, AddDeviceActivity::class.java))
        }

        binding.cardRegisterDevice.setOnClickListener {
            startActivity(Intent(this, AddDeviceActivity::class.java))
        }
    }

    private fun setupTopBar() {
        val avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=1000"
        binding.avatarImage.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_devices
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeClientActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, RequestsClientActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_repairs -> {
                    startActivity(Intent(this, RepairsClientActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_devices -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileClientActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
