package com.cibertec.cibertecapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ServiceRequest(
    val id: String = "ORD-${(1000..9999).random()}${( 'A'..'Z').random()}",
    val deviceName: String,
    val category: String,
    val problemType: String,
    val description: String,
    val status: String = "RECIBIDO",
    val progress: Int = 0,
    val technicianName: String = "Pendiente de Asignación",
    val date: String = "Hoy",
    val iconRes: Int,
    val color: String = "#FFB300",
    val price: Double = 0.0,
    val isPaid: Boolean = false
)

data class UserDevice(
    val id: String,
    val name: String,
    val serial: String,
    val category: String,
    val iconRes: Int,
    val status: String = "Garantía Activa"
)

object ServiceRepository {
    val serviceList = mutableListOf<ServiceRequest>()
    val deviceList = mutableListOf<UserDevice>()
    
    private const val PREFS_NAME = "reparatech_persistent_storage"
    private const val KEY_SERVICES = "services_json"
    private const val KEY_DEVICES = "devices_json"
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Cargar Servicios
        val servicesJson = prefs.getString(KEY_SERVICES, null)
        if (servicesJson != null) {
            try {
                val jsonArray = JSONArray(servicesJson)
                serviceList.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    serviceList.add(ServiceRequest(
                        id = obj.getString("id"),
                        deviceName = obj.getString("deviceName"),
                        category = obj.getString("category"),
                        problemType = obj.optString("problemType", "General"),
                        description = obj.getString("description"),
                        status = obj.getString("status"),
                        progress = obj.getInt("progress"),
                        technicianName = obj.getString("technicianName"),
                        date = obj.getString("date"),
                        iconRes = obj.getInt("iconRes"),
                        color = obj.getString("color"),
                        price = obj.optDouble("price", 0.0),
                        isPaid = obj.optBoolean("isPaid", false)
                    ))
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            loadDefaultServices()
        }

        // Cargar Dispositivos
        val devicesJson = prefs.getString(KEY_DEVICES, null)
        if (devicesJson != null) {
            try {
                val jsonArray = JSONArray(devicesJson)
                deviceList.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    deviceList.add(UserDevice(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        serial = obj.getString("serial"),
                        category = obj.getString("category"),
                        iconRes = obj.getInt("iconRes"),
                        status = obj.getString("status")
                    ))
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            loadDefaultDevices()
        }
        
        isInitialized = true
    }

    private fun loadDefaultServices() {
        serviceList.clear()
        
        // Phone 17 Pro Max - CATEGORÍA CELULARES (Obligatorio)
        serviceList.add(ServiceRequest(
            id = "ORD-8531L",
            deviceName = "Phone 17 Pro Max",
            category = "Celulares",
            problemType = "Cambio de Pantalla",
            description = "Falla de táctil y cristal roto.",
            status = "EN PROCESO",
            progress = 10,
            technicianName = "Ing. Ricardo Palma",
            date = "Hoy",
            iconRes = R.drawable.ic_smartphone,
            color = "#003D9B",
            price = 450.0,
            isPaid = true
        ))

        // Laptop Dell XPS 15 - CATEGORÍA LAPTOPS
        serviceList.add(ServiceRequest(
            id = "ORD-3938S",
            deviceName = "Laptop Dell XPS 15",
            category = "Laptops",
            problemType = "Limpieza General",
            description = "Mantenimiento preventivo.",
            status = "RECIBIDO",
            progress = 0,
            technicianName = "Pendiente de Asignación",
            date = "Mañana",
            iconRes = R.drawable.ic_laptop,
            color = "#FF9500",
            price = 150.0,
            isPaid = false
        ))
    }

    private fun loadDefaultDevices() {
        deviceList.clear()
        deviceList.add(UserDevice(UUID.randomUUID().toString(), "Phone 17 Pro Max", "AP-PH17-001", "Celulares", R.drawable.ic_smartphone))
        deviceList.add(UserDevice(UUID.randomUUID().toString(), "Laptop Dell XPS 15", "DL-XPS-9520", "Laptops", R.drawable.ic_laptop))
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val servicesArray = JSONArray()
        serviceList.forEach { request ->
            servicesArray.put(JSONObject().apply {
                put("id", request.id)
                put("deviceName", request.deviceName)
                put("category", request.category)
                put("problemType", request.problemType)
                put("description", request.description)
                put("status", request.status)
                put("progress", request.progress)
                put("technicianName", request.technicianName)
                put("date", request.date)
                put("iconRes", request.iconRes)
                put("color", request.color)
                put("price", request.price)
                put("isPaid", request.isPaid)
            })
        }
        editor.putString(KEY_SERVICES, servicesArray.toString())

        val devicesArray = JSONArray()
        deviceList.forEach { device ->
            devicesArray.put(JSONObject().apply {
                put("id", device.id)
                put("name", device.name)
                put("serial", device.serial)
                put("category", device.category)
                put("iconRes", device.iconRes)
                put("status", device.status)
            })
        }
        editor.putString(KEY_DEVICES, devicesArray.toString())
        editor.apply()
    }
}