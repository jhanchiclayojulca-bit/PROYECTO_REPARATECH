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
import com.cibertec.cibertecapp.databinding.ActivityHomeClientBinding

class HomeClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupTopBar()
        setupBottomNavigation()
        setupListeners()
        setupCategoryFilters()
        refreshRecentRepairs()
    }

    override fun onResume() {
        super.onResume()
        refreshRecentRepairs()
    }

    private fun refreshRecentRepairs() {
        binding.repairsContainer.removeAllViews()
        
        // Mostrar los 3 servicios más recientes
        val recentRepairs = ServiceRepository.serviceList.take(3)
        
        if (recentRepairs.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No tienes reparaciones activas"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 40, 0, 0)
                alpha = 0.5f
            }
            binding.repairsContainer.addView(emptyText)
        } else {
            recentRepairs.forEach { repair ->
                val repairView = layoutInflater.inflate(R.layout.item_repair_card, binding.repairsContainer, false)
                
                val name = repairView.findViewById<TextView>(R.id.tvRepairName)
                val id = repairView.findViewById<TextView>(R.id.tvRepairId)
                val status = repairView.findViewById<TextView>(R.id.tvRepairStatus)
                val progress = repairView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.repairProgress)
                val date = repairView.findViewById<TextView>(R.id.tvDeliveryDate)
                val icon = repairView.findViewById<ImageView>(R.id.ivRepairIcon)

                name.text = repair.deviceName
                id.text = repair.id
                status.text = repair.status
                progress.progress = repair.progress
                date.text = "Entrega estimada: ${repair.date}"
                icon.setImageResource(repair.iconRes)

                binding.repairsContainer.addView(repairView)
            }
        }
    }

    private fun setupCategoryFilters() {
        binding.btnViewAll.setOnClickListener {
            showAllRepairs()
        }

        binding.catLaptop.setOnClickListener { showProducts("Laptops") }
        binding.catCelulares.setOnClickListener { showProducts("Celulares") }
        binding.catGaming.setOnClickListener { showProducts("Gaming") }
        binding.catTablets.setOnClickListener { showProducts("Tablets") }

        binding.btnBackHome.setOnClickListener {
            showHome()
        }
    }

    private fun showAllRepairs() {
        binding.tvSectionTitle.text = "Todas tus Reparaciones"
        binding.repairsContainer.visibility = View.GONE
        binding.homeSectionsContainer.visibility = View.GONE
        binding.productsContainer.visibility = View.VISIBLE
        binding.btnBackContainer.visibility = View.VISIBLE
        binding.btnViewAll.visibility = View.GONE

        binding.productsContainer.removeAllViews()

        ServiceRepository.serviceList.forEach { repair ->
            val repairView = layoutInflater.inflate(R.layout.item_repair_card, binding.productsContainer, false)
            val name = repairView.findViewById<TextView>(R.id.tvRepairName)
            val id = repairView.findViewById<TextView>(R.id.tvRepairId)
            val status = repairView.findViewById<TextView>(R.id.tvRepairStatus)
            val progress = repairView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.repairProgress)
            val date = repairView.findViewById<TextView>(R.id.tvDeliveryDate)
            val icon = repairView.findViewById<ImageView>(R.id.ivRepairIcon)

            name.text = repair.deviceName
            id.text = repair.id
            status.text = repair.status
            progress.progress = repair.progress
            date.text = "Entrega estimada: ${repair.date}"
            icon.setImageResource(repair.iconRes)

            binding.productsContainer.addView(repairView)
        }
    }

    private fun showProducts(category: String) {
        binding.tvSectionTitle.text = "Reparaciones de $category"
        binding.repairsContainer.visibility = View.GONE
        binding.homeSectionsContainer.visibility = View.GONE
        binding.productsContainer.visibility = View.VISIBLE
        binding.btnBackContainer.visibility = View.VISIBLE
        binding.btnViewAll.visibility = View.GONE
        
        binding.productsContainer.removeAllViews()
        
        val filtered = ServiceRepository.serviceList.filter { it.category == category }

        if (filtered.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Sin registros en esta categoría"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 100, 0, 0)
                textSize = 16f
                alpha = 0.5f
            }
            binding.productsContainer.addView(emptyText)
        } else {
            filtered.forEach { repair ->
                val repairView = layoutInflater.inflate(R.layout.item_repair_card, binding.productsContainer, false)
                val name = repairView.findViewById<TextView>(R.id.tvRepairName)
                val id = repairView.findViewById<TextView>(R.id.tvRepairId)
                val status = repairView.findViewById<TextView>(R.id.tvRepairStatus)
                val progress = repairView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.repairProgress)
                val date = repairView.findViewById<TextView>(R.id.tvDeliveryDate)
                val icon = repairView.findViewById<ImageView>(R.id.ivRepairIcon)

                name.text = repair.deviceName
                id.text = repair.id
                status.text = repair.status
                progress.progress = repair.progress
                date.text = "Entrega estimada: ${repair.date}"
                icon.setImageResource(repair.iconRes)

                binding.productsContainer.addView(repairView)
            }
        }
    }

    private fun showHome() {
        binding.tvSectionTitle.text = "Tus Reparaciones"
        binding.repairsContainer.visibility = View.VISIBLE
        binding.homeSectionsContainer.visibility = View.VISIBLE
        binding.productsContainer.visibility = View.GONE
        binding.btnBackContainer.visibility = View.GONE
        binding.btnViewAll.visibility = View.VISIBLE
        refreshRecentRepairs()
    }

    private fun setupListeners() {
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "No tienes notificaciones nuevas", Toast.LENGTH_SHORT).show()
        }

        binding.btnMyDevices.setOnClickListener {
            startActivity(Intent(this, DevicesClientActivity::class.java))
        }
        
        binding.btnNewRequest.setOnClickListener {
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
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesClientActivity::class.java))
                    finish()
                    true
                }
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
