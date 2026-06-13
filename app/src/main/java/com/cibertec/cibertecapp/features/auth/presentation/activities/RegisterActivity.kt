package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.databinding.ActivityRegisterBinding
import com.cibertec.cibertecapp.features.auth.presentation.viewmodels.RegisterViewModel
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvLoginLink.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            if (validateFields()) {
                val name = binding.etName.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val pass = binding.etPassword.text.toString().trim()
                viewModel.register(name, phone, email, pass)
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        // Validar Nombre
        if (name.isEmpty()) {
            binding.tilName.error = "Ingresa tu nombre completo"
            isValid = false
        } else if (name.length < 3) {
            binding.tilName.error = "El nombre es demasiado corto"
            isValid = false
        } else if (name.any { it.isDigit() }) {
            binding.tilName.error = "El nombre no puede contener números"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Validar Teléfono
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Ingresa tu número de teléfono"
            isValid = false
        } else if (phone.length != 9) {
            binding.tilPhone.error = "El teléfono debe tener 9 dígitos"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        // Validar Email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingresa tu correo electrónico"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Formato de correo inválido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validar Contraseña
        if (pass.isEmpty()) {
            binding.tilPassword.error = "Ingresa una contraseña"
            isValid = false
        } else if (pass.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnRegister.isEnabled = !state.isLoading

                if (state.isSuccess) {
                    Toast.makeText(this@RegisterActivity, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finishAffinity()
                }

                state.error?.let {
                    Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
