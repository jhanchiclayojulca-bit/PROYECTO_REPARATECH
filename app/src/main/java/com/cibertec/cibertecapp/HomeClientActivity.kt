package com.cibertec.cibertecapp

import android.os.Bundle
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
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_devices -> {
                    startActivity(android.content.Intent(this, DevicesClientActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(android.content.Intent(this, ProfileClientActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
