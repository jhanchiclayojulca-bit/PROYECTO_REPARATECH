package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.databinding.ActivityProfileClientBinding

class ProfileClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        setupBottomNavigation()
    }

    private fun setupTopBar() {
        // Imágenes Premium en Ultra HD (4K) desde Unsplash
        val avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=1000"
        val coverUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=2048"
        
        binding.avatarImage.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        binding.profileAvatarLarge.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        binding.coverImage.load(coverUrl) {
            crossfade(true)
        }

        binding.logoutButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeClientActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesClientActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
