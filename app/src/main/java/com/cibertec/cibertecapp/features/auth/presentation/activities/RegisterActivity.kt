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
                val phone = binding.etPhone.text.toString().replace("\\s".toRegex(), "")
                val email = binding.etEmail.text.toString().trim().lowercase()
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
        val confirmPass = binding.etConfirmPassword.text.toString().trim()

        // 1. Validar Nombre (Solo letras y espacios, max 50)
        val nameRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()
        if (name.isEmpty()) {
            binding.tilName.error = "Ingresa tu nombre completo"
            isValid = false
        } else if (name.length < 3) {
            binding.tilName.error = "El nombre es demasiado corto"
            isValid = false
        } else if (name.length > 50) {
            binding.tilName.error = "Máximo 50 caracteres"
            isValid = false
        } else if (!name.matches(nameRegex)) {
            binding.tilName.error = "El nombre solo debe contener letras"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // 2. Validar Teléfono (Limpieza y formato Peruano)
        val cleanPhone = phone.replace("\\s".toRegex(), "").replace("-", "").replace("(", "").replace(")", "")
        if (cleanPhone.isEmpty()) {
            binding.tilPhone.error = "Ingresa tu número de teléfono"
            isValid = false
        } else if (cleanPhone.length != 9 || !cleanPhone.all { it.isDigit() }) {
            binding.tilPhone.error = "Deben ser 9 dígitos numéricos"
            isValid = false
        } else if (!cleanPhone.startsWith("9")) {
            binding.tilPhone.error = "Debe empezar con 9"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        // 3. Validar Email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingresa tu correo electrónico"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            binding.tilEmail.error = "Formato de correo inválido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // 4. Validar Contraseña (Fuerza: Letra + Número, min 6)
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d).+$".toRegex()
        if (pass.isEmpty()) {
            binding.tilPassword.error = "Ingresa una contraseña"
            isValid = false
        } else if (pass.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else if (!pass.matches(passwordRegex)) {
            binding.tilPassword.error = "Debe incluir letras y números"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // 5. Confirmar Contraseña
        if (confirmPass.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirma tu contraseña"
            isValid = false
        } else if (confirmPass != pass) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.loadingOverlay.visibility = if (state.isLoading) View.VISIBLE else View.GONE
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
