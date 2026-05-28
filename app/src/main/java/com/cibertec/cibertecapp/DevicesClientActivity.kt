package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
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

        setupTopBar()
        setupBottomNavigation()
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
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_devices -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileClientActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
