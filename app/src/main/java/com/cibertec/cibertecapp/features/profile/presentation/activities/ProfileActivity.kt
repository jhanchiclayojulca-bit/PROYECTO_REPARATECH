package com.cibertec.cibertecapp.features.profile.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.features.support.presentation.activities.SupportActivity
import com.cibertec.cibertecapp.databinding.ActivityProfileClientBinding
import com.cibertec.cibertecapp.features.auth.presentation.activities.LoginActivity
import com.cibertec.cibertecapp.features.profile.presentation.viewmodels.ProfileViewModel
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairsActivity
import com.cibertec.cibertecapp.features.devices.presentation.activities.DevicesActivity
import com.cibertec.cibertecapp.features.requests.presentation.activities.RequestsActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileClientBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupProfileOptions()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.tvProfileName.text = state.name
                binding.tvTotalRepairs.text = state.totalRepairs.toString()
                binding.tvTotalDevices.text = state.totalDevices.toString()
                
                binding.profileAvatarLarge.load(state.avatarUrl) {
                    crossfade(true)
                    placeholder(R.mipmap.ic_launcher)
                    error(R.mipmap.ic_launcher)
                    transformations(CircleCropTransformation())
                }

                if (state.isLoggedOut) {
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupProfileOptions() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.optionAccountInfo.root.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }
        binding.optionAccountInfo.optionIcon.setImageResource(R.drawable.ic_person)
        binding.optionAccountInfo.optionTitle.text = "Información Personal"
        
        binding.optionSecurity.root.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }
        binding.optionSecurity.optionIcon.setImageResource(R.drawable.ic_security)
        binding.optionSecurity.optionTitle.text = "Seguridad y Contraseña"

        binding.optionRequests.root.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }
        binding.optionRequests.optionIcon.setImageResource(R.drawable.ic_notifications_none)
        binding.optionRequests.optionTitle.text = "Mis Solicitudes de Soporte"

        binding.optionHelp.root.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }
        binding.optionHelp.optionIcon.setImageResource(R.drawable.ic_help)
        binding.optionHelp.optionTitle.text = "Centro de Ayuda"

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, RequestsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_repairs -> {
                    startActivity(Intent(this, RepairsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
