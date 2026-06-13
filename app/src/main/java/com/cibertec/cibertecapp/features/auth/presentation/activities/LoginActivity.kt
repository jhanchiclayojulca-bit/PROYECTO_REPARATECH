package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityLoginBinding
import com.cibertec.cibertecapp.features.auth.presentation.viewmodels.LoginViewModel
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.loginWithGoogle(it) }
        } catch (e: ApiException) {
            Toast.makeText(this, "Error de Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleClient()
        setupListeners()
        observeState()
    }

    private fun setupGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        binding.btnlogin.setOnClickListener {
            validateLogin()
        }
        binding.btnregister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val emailInput = android.widget.EditText(this)
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(60, 20, 60, 20)
        emailInput.layoutParams = params
        emailInput.hint = "tu@correo.com"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        container.addView(emailInput)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Recuperar Contraseña")
            .setMessage("Ingresa tu correo electrónico para enviarte un enlace de recuperación.")
            .setView(container)
            .setPositiveButton("Enviar") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Enlace enviado. Revisa SPAM y tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                            } else {
                                val errorMsg = task.exception?.message ?: "Error desconocido"
                                Log.e("FirebaseError", "Error al enviar: $errorMsg")
                                Toast.makeText(this, "Firebase dice: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validateLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingrese su correo"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Correo inválido"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Ingrese su contraseña"
            return
        }

        viewModel.login(email, password)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.btnlogin.isEnabled = !state.isLoading
                binding.btnGoogleLogin.isEnabled = !state.isLoading

                if (state.isLoading) {
                    binding.btnlogin.text = "Cargando..."
                } else {
                    binding.btnlogin.text = "Entrar"
                }

                if (state.isSuccess) {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }

                state.error?.let {
                    Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
