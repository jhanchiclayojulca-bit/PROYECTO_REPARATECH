package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.databinding.ActivityRequestsClientBinding

class RequestsClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupTopBar()
        setupBottomNavigation()
        populateRequests()
    }

    override fun onResume() {
        super.onResume()
        populateRequests()
    }

    private fun populateRequests() {
        binding.requestsContainer.removeAllViews()
        
        ServiceRepository.serviceList.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_request_card, binding.requestsContainer, false)
            
            val id = itemView.findViewById<TextView>(R.id.tvRequestId)
            val category = itemView.findViewById<TextView>(R.id.tvRequestCategory)
            val title = itemView.findViewById<TextView>(R.id.tvRequestTitle)
            val desc = itemView.findViewById<TextView>(R.id.tvRequestDesc)
            val status = itemView.findViewById<TextView>(R.id.tvRequestStatusLabel)
            val date = itemView.findViewById<TextView>(R.id.tvRequestDate)
            val indicator = itemView.findViewById<View>(R.id.statusIndicator)

            id.text = "#${item.id}"
            category.text = item.category.uppercase()
            title.text = item.deviceName
            desc.text = item.description
            status.text = item.status
            date.text = item.date
            
            try {
                indicator.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor(item.color)
                )
            } catch (e: Exception) {
                // Color por defecto si falla
            }

            binding.requestsContainer.addView(itemView)
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
        binding.bottomNavigation.selectedItemId = R.id.nav_requests
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeClientActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_requests -> true
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
