package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.databinding.ActivityLoginBinding
import com.cibertec.cibertecapp.features.auth.presentation.viewmodels.LoginViewModel
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupListeners()

        observeState()
    }

    private fun setupListeners(){
        binding.btnlogin.setOnClickListener {
            validateLogin()
        }
    }

    private fun validateLogin() {

        val email = binding.etEmail.text.toString().trim()

        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // VALIDAR TODOS LOS CAMPOS
        if (email.isEmpty() && password.isEmpty()) {

            binding.tilEmail.error = "Ingrese su correo"

            binding.tilPassword.error = "Ingrese su contraseña"

            binding.etEmail.requestFocus()

            return
        }

        // VALIDAR EMAIL VACÍO
        if (email.isEmpty()) {

            binding.tilEmail.error = "Ingrese su correo electrónico"

            binding.etEmail.requestFocus()

            return
        }

        // VALIDAR LONGITUD EMAIL
        if (email.length > 50) {

            binding.tilEmail.error = "El correo es demasiado largo"

            binding.etEmail.requestFocus()

            return
        }

        // VALIDAR FORMATO EMAIL
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.tilEmail.error = "Ingrese un correo válido"

            binding.etEmail.requestFocus()

            return
        }

        // VALIDAR PASSWORD VACÍO
        if (password.isEmpty()) {

            binding.tilPassword.error = "Ingrese su contraseña"

            binding.etPassword.requestFocus()

            return
        }

        // VALIDAR ESPACIOS
        if (password.contains(" ")) {

            binding.tilPassword.error = "La contraseña no debe contener espacios"

            binding.etPassword.requestFocus()

            return
        }

        // VALIDAR LONGITUD MÍNIMA
        if (password.length < 8) {

            binding.tilPassword.error =
                "La contraseña debe tener mínimo 8 caracteres"

            binding.etPassword.requestFocus()

            return
        }

        // VALIDAR MAYÚSCULA
        if (!password.any { it.isUpperCase() }) {

            binding.tilPassword.error =
                "Debe contener al menos una letra mayúscula"

            binding.etPassword.requestFocus()

            return
        }

        // VALIDAR NÚMERO
        if (!password.any { it.isDigit() }) {

            binding.tilPassword.error =
                "Debe contener al menos un número"

            binding.etPassword.requestFocus()

            return
        }

        // VALIDAR CARÁCTER ESPECIAL
        val specialCharacters = "@#$%^&+=!"

        if (!password.any { specialCharacters.contains(it) }) {

            binding.tilPassword.error =
                "Debe contener un carácter especial"

            binding.etPassword.requestFocus()

            return
        }

        // TODO CORRECTO
        viewModel.login(
            email,
            password
        )
    }

    private fun observeState(){
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.btnlogin.isEnabled = !state.isLoading

                if (state.isLoading){
                    binding.btnlogin.text = "Entrando..."
                }else{
                    binding.btnlogin.text = "Entrar"
                }

                if (state.isSuccess){
                    Toast.makeText(
                        this@LoginActivity,
                        "Login exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(
                            this@LoginActivity,
                            HomeActivity::class.java
                        )
                    )

                    finish()
                }
                state.error?.let {
                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

}