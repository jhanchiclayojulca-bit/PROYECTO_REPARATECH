package com.cibertec.cibertecapp.features.profile.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivitySecurityBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SecurityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthProvider()
        setupToolbar()
        setupListeners()
    }

    private fun checkAuthProvider() {
        val user = auth.currentUser
        val providers = user?.providerData?.map { it.providerId } ?: emptyList()
        

        if (providers.contains("google.com") && !providers.contains("password")) {
            // Ocultar sección de contraseña
            binding.tvChangePasswordTitle.visibility = View.GONE
            binding.cardChangePassword.visibility = View.GONE
            binding.btnUpdatePassword.visibility = View.GONE
            
            // Mostrar mensaje informativo
            val infoText = android.widget.TextView(this).apply {
                text = "Tu seguridad está gestionada por Google. No es necesario gestionar una contraseña local en ReparaTech."
                setPadding(0, 20, 0, 40)
                setTextColor(getColor(com.cibertec.cibertecapp.R.color.on_surface_variant))
                textSize = 14f
            }
            (binding.cardChangePassword.parent as android.widget.LinearLayout).addView(infoText, 1)
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.btnUpdatePassword.setOnClickListener {
            updatePassword()
        }

        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun updatePassword() {
        val currentPass = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)
            
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteAccountConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar cuenta?")
            .setMessage("Esta acción es irreversible. Se perderán todos tus datos y reparaciones.")
            .setPositiveButton("Eliminar") { _, _ ->
                Toast.makeText(this, "Función restringida por seguridad", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
