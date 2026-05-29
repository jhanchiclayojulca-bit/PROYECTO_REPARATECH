package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.databinding.ActivityProfileClientBinding
import com.cibertec.cibertecapp.features.auth.presentation.activities.LoginActivity

class ProfileClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupTopBar()
        setupBottomNavigation()
        setupProfileOptions()
    }

    private fun setupProfileOptions() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Opción: Mi Información
        binding.optionAccountInfo.root.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }
        binding.optionAccountInfo.optionIcon.setImageResource(R.drawable.ic_person)
        binding.optionAccountInfo.optionTitle.text = "Información Personal"
        
        // Opción: Seguridad
        binding.optionSecurity.optionIcon.setImageResource(R.drawable.ic_security)
        binding.optionSecurity.optionTitle.text = "Seguridad y Contraseña"

        // Opción: Mis Solicitudes
        binding.optionRequests.optionIcon.setImageResource(R.drawable.ic_notifications_none)
        binding.optionRequests.optionTitle.text = "Mis Solicitudes de Soporte"

        // Opción: Centro de Ayuda
        binding.optionHelp.optionIcon.setImageResource(R.drawable.ic_help)
        binding.optionHelp.optionTitle.text = "Centro de Ayuda"
    }

    private fun setupTopBar() {
        // Imágenes Premium en Ultra HD (4K) desde Unsplash
        val avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=1000"
        
        binding.profileAvatarLarge.load(avatarUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
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
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesClientActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
