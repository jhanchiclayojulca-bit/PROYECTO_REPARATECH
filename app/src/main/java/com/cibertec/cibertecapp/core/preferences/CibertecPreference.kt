package com.cibertec.cibertecapp.core.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CibertecPreference(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val storage: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "app_prefs_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Onboarding
    fun setOnboardingFinished(finished: Boolean) {
        storage.edit().putBoolean("onboarding_finished", finished).apply()
    }

    fun isOnboardingFinished(): Boolean {
        return storage.getBoolean("onboarding_finished", false)
    }

    // Login Credentials (Remember Me)
    fun saveCredentials(email: String, remember: Boolean) {
        storage.edit().apply {
            putString("saved_email", email)
            putBoolean("remember_me", remember)
        }.apply()
    }

    fun getSavedEmail(): String? = storage.getString("saved_email", "")
    fun isRememberMeActive(): Boolean = storage.getBoolean("remember_me", false)

    fun clearCredentials() {
        storage.edit().apply {
            remove("saved_email")
            putBoolean("remember_me", false)
        }.apply()
    }

    // User Profile Cache
    fun saveUserSession(name: String, email: String?, photo: String?, phone: String? = null) {
        storage.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_photo", photo)
            putString("user_phone", phone)
        }.apply()
    }

    fun getUserName(): String = storage.getString("user_name", "Usuario") ?: "Usuario"
    fun getUserPhoto(): String? = storage.getString("user_photo", null)
    fun getUserPhone(): String = storage.getString("user_phone", "") ?: ""

    // Settings
    fun setDarkMode(enabled: Boolean) {
        storage.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean = storage.getBoolean("dark_mode", false)

    fun setBiometricsEnabled(enabled: Boolean) {
        storage.edit().putBoolean("biometrics_enabled", enabled).apply()
    }

    fun isBiometricsEnabled(): Boolean = storage.getBoolean("biometrics_enabled", false)
}
