package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ServiceRepository.init(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnlogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showValidationError(
                        "Campos Obligatorios",
                        "Por favor, complete todos los campos para continuar."
                    )
                }
                !isValidEmail(email) -> {
                    showValidationError(
                        "Formato Inválido",
                        "El correo electrónico ingresado no tiene un formato válido."
                    )
                }
                !isSecurePassword(password) -> {
                    showValidationError(
                        "Contraseña Débil",
                        "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, un número y un carácter especial."
                    )
                }
                else -> {
                    val validEmail = "reparatech12@gmail.com"
                    val validPassword = "Repara2026!"

                    if (email == validEmail && password == validPassword) {
                        // Login exitoso
                        val intent = Intent(this, HomeClientActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showValidationError(
                            "Credenciales Incorrectas",
                            "El correo electrónico o la contraseña son incorrectos. Por favor, inténtelo de nuevo."
                        )
                    }
                }
            }
        }

        binding.btnregister.setOnClickListener {
            showValidationError(
                "Próximamente",
                "La función de registro estará disponible pronto."
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isSecurePassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(Regex(passwordPattern))
    }

    private fun showValidationError(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Entendido") { dialog, _ -> 
                dialog.dismiss() 
            }
            .show()
    }
}
