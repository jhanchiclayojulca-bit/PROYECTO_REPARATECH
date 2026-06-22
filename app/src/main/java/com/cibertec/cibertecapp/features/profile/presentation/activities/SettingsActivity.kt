package com.cibertec.cibertecapp.features.profile.presentation.activities

import android.content.Context
import android.os.Bundle
import com.cibertec.cibertecapp.core.preferences.CibertecPreference
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.cibertec.cibertecapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadSettings() {
        val prefs = CibertecPreference(this)
        
        // Cargar Modo Oscuro
        binding.switchDarkMode.isChecked = prefs.isDarkModeEnabled()

        // Cargar Biometría
        binding.switchBiometrics.isChecked = prefs.isBiometricsEnabled()
    }

    private fun setupListeners() {
        val prefs = CibertecPreference(this)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkMode(isChecked)
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            prefs.setBiometricsEnabled(isChecked)
        }
    }
}
