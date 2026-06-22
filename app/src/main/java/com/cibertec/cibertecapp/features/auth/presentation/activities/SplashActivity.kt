package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.cibertec.cibertecapp.core.preferences.CibertecPreference
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2000)
    }

    private fun applyTheme() {
        val prefs = CibertecPreference(this)
        val isDarkMode = prefs.isDarkModeEnabled()
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun checkSession() {
        val auth = FirebaseAuth.getInstance()
        val prefs = CibertecPreference(this)
        val onboardingFinished = prefs.isOnboardingFinished()

        if (!onboardingFinished) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        } else if (auth.currentUser != null) {
            val biometricsEnabled = prefs.isBiometricsEnabled()
            if (biometricsEnabled) {
                tryBiometricAuth()
            } else {
                navigateToHome()
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun tryBiometricAuth() {
        val biometricManager = BiometricManager.from(this)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            else -> {
                // Si el dispositivo no tiene biometría o no está configurada, entramos directo
                navigateToHome()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Si hay error (o el usuario cancela), lo mandamos al Login por seguridad
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToHome()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Seguro")
            .setSubtitle("Usa tu huella para entrar a ReparaTech")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
