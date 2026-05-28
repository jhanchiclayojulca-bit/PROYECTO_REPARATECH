package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.databinding.ActivityLoginBinding
import com.cibertec.cibertecapp.features.auth.presentation.viewmodels.LoginViewModel
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

    private fun validateLogin(){
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()){
            binding.tilEmail.error = "Ingrese su correo"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.tilEmail.error = "Correo inválido"
            return
        }
        if (password.isEmpty()){
            binding.tilPassword.error = "Ingrese su contraseña"
            return
        }
        if (password.length < 6){
            binding.tilPassword.error = "Minimo 6 caracteres"
            return
        }
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
                    Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                }
                state.error?.let {
                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

}