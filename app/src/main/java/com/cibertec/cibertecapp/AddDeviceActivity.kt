package com.cibertec.cibertecapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityAddDeviceBinding
import java.util.UUID

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDeviceBinding
    private var isEditMode = false
    private var deviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        deviceId = intent.getStringExtra("DEVICE_ID")

        setupProblemTypeDropdown()
        setupListeners()

        if (isEditMode) {
            setupEditMode()
        }
    }

    private fun setupEditMode() {
        binding.topAppBar.title = "Editar Equipo"
        binding.btnRegister.text = "Guardar Cambios"
        
        val device = ServiceRepository.deviceList.find { it.id == deviceId }
        device?.let {
            binding.etDeviceName.setText(it.name)
            binding.atvProblemType.setText(it.category, false)
            // Llenar marca y modelo (usamos el nombre completo para la demo)
            binding.etBrand.setText(it.name.split(" ").firstOrNull() ?: "")
            binding.etModel.setText(it.name.substringAfter(" ", ""))
            binding.etSerial.setText(it.serial)
        }
    }

    private fun setupProblemTypeDropdown() {
        val deviceTypes = arrayOf(
            "Laptops",
            "Celulares",
            "Gaming",
            "Tablets"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, deviceTypes)
        binding.atvProblemType.setAdapter(adapter)
        binding.atvProblemType.setText(deviceTypes[0], false)
    }

    private fun setupListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etDeviceName.text.toString().trim()
            val type = binding.atvProblemType.text.toString()
            val brand = binding.etBrand.text.toString().trim()
            val model = binding.etModel.text.toString().trim()
            val serial = binding.etSerial.text.toString().trim()

            if (name.isEmpty() || brand.isEmpty() || model.isEmpty() || serial.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                if (isEditMode) {
                    updateDevice(name, type, brand, model, serial)
                } else {
                    addNewDevice(name, type, brand, model, serial)
                }
                ServiceRepository.save(this)
                finish()
            }
        }
    }

    private fun addNewDevice(name: String, type: String, brand: String, model: String, serial: String) {
        val iconRes = when(type) {
            "Laptops" -> R.drawable.ic_laptop
            "Celulares" -> R.drawable.ic_smartphone
            "Gaming" -> R.drawable.ic_gaming
            else -> R.drawable.ic_devices
        }

        ServiceRepository.deviceList.add(
            UserDevice(
                id = UUID.randomUUID().toString(),
                name = name,
                serial = serial,
                category = type,
                iconRes = iconRes
            )
        )
        Toast.makeText(this, "¡Dispositivo registrado con éxito!", Toast.LENGTH_LONG).show()
    }

    private fun updateDevice(name: String, type: String, brand: String, model: String, serial: String) {
        val index = ServiceRepository.deviceList.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            val oldDevice = ServiceRepository.deviceList[index]
            val iconRes = when(type) {
                "Laptops" -> R.drawable.ic_laptop
                "Celulares" -> R.drawable.ic_smartphone
                "Gaming" -> R.drawable.ic_gaming
                else -> R.drawable.ic_devices
            }

            ServiceRepository.deviceList[index] = oldDevice.copy(
                name = name,
                serial = serial,
                category = type,
                iconRes = iconRes
            )
            Toast.makeText(this, "Equipo actualizado con éxito", Toast.LENGTH_SHORT).show()
        }
    }
}
