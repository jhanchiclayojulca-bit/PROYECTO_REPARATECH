package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.databinding.ActivityRepairsClientBinding
import com.cibertec.cibertecapp.databinding.ItemRepairModernCardBinding

class RepairsClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRepairsClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepairsClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupTopBar()
        setupBottomNavigation()
        populateRepairs()
    }

    override fun onResume() {
        super.onResume()
        populateRepairs()
    }

    private fun populateRepairs() {
        binding.repairsContainer.removeAllViews()
        
        val unpaidCount = ServiceRepository.serviceList.count { !it.isPaid }
        val totalUnpaid = ServiceRepository.serviceList.filter { !it.isPaid }.sumOf { it.price }

        if (unpaidCount > 0) {
            binding.tvBannerTitle.text = "Pagos Pendientes"
            binding.tvBannerDesc.text = "Tienes $unpaidCount servicios esperando pago por un total de S/ ${String.format("%.2f", totalUnpaid)}. Paga ahora para iniciar la reparación."
        } else {
            binding.tvBannerTitle.text = "Seguimiento Maestro"
            binding.tvBannerDesc.text = "Monitorea el progreso de tus equipos en tiempo real con nuestra tecnología de punta."
        }

        ServiceRepository.serviceList.forEach { repair ->
            val itemBinding = ItemRepairModernCardBinding.inflate(layoutInflater, binding.repairsContainer, false)
            
            itemBinding.tvRepairName.text = repair.deviceName
            itemBinding.ivRepairIcon.setImageResource(repair.iconRes)
            
            // UI state based on payment
            if (!repair.isPaid) {
                // Not paid: Show Orange Payment Button
                itemBinding.btnPayRepair.visibility = View.VISIBLE
                itemBinding.btnPayRepair.text = "Realizar Pago (S/ ${String.format("%.2f", repair.price)})"
                
                itemBinding.tvRepairStatus.text = "PENDIENTE"
                itemBinding.repairProgress.progress = 0
                itemBinding.tvProgressText.text = "0%"
                
                // Hide tech info until paid
                itemBinding.dividerTech.visibility = View.GONE
                itemBinding.techInfoContainer.visibility = View.GONE
                
                itemBinding.btnPayRepair.setOnClickListener {
                    val intent = Intent(this, PaymentActivity::class.java)
                    intent.putExtra("ORDER_ID", repair.id)
                    startActivity(intent)
                }
            } else {
                // Paid: Hide Button, Show Progress and Tech
                itemBinding.btnPayRepair.visibility = View.GONE
                
                itemBinding.tvRepairStatus.text = repair.status
                itemBinding.repairProgress.progress = repair.progress
                itemBinding.tvProgressText.text = "${repair.progress}%"
                
                itemBinding.dividerTech.visibility = View.VISIBLE
                itemBinding.techInfoContainer.visibility = View.VISIBLE
                
                itemBinding.tvTechnicianName.text = repair.technicianName
                val techAvatar = "https://i.pravatar.cc/150?u=${repair.technicianName.hashCode()}"
                itemBinding.ivTechAvatar.load(techAvatar) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    transformations(CircleCropTransformation())
                }
            }

            binding.repairsContainer.addView(itemBinding.root)
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
        binding.bottomNavigation.selectedItemId = R.id.nav_repairs
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
                R.id.nav_repairs -> true
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