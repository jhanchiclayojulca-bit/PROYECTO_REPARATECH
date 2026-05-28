package com.cibertec.cibertecapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cibertec.cibertecapp.databinding.ActivityLoginBinding
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var selectedRole: String = "Cliente"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI with default role
        updateRoleUI(binding.btnRoleClient, binding.btnRoleAdmin)

        setupRoleSelector()
        setupListeners()
    }

    private fun setupRoleSelector() {
        binding.btnRoleClient.setOnClickListener {
            updateRoleUI(binding.btnRoleClient, binding.btnRoleAdmin)
            selectedRole = "Cliente"
        }

        binding.btnRoleAdmin.setOnClickListener {
            updateRoleUI(binding.btnRoleAdmin, binding.btnRoleClient)
            selectedRole = "Administrador"
        }
    }

    private fun updateRoleUI(selected: MaterialButton, unselected: MaterialButton) {
        // Selected Button Styles
        selected.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.login_primary))
        selected.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Unselected Button Styles
        unselected.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        unselected.setTextColor(ContextCompat.getColor(this, R.color.login_on_surface_variant))
    }

    private fun setupListeners() {
        binding.btnlogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedRole == "Cliente") {
                    val intent = Intent(this, HomeClientActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Iniciando sesión como Administrador (En desarrollo)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnregister.setOnClickListener {
            Toast.makeText(this, "Navegando al registro...", Toast.LENGTH_SHORT).show()
        }
    }
}
