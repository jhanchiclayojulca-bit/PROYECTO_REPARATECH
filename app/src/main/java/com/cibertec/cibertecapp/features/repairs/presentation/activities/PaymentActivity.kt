package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent

class PaymentActivity : AppCompatActivity() {

    private var hasLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manejar el regreso automático del Deep Link
        if (intent?.data?.scheme == "reparatech") {
            handleReturn(intent.data)
            return
        }

        val paymentUrl = intent.getStringExtra("PAYMENT_URL") ?: ""
        if (paymentUrl.isNotEmpty() && !hasLaunched) {
            hasLaunched = true
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(this, Uri.parse(paymentUrl))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleReturn(intent.data)
    }

    private fun handleReturn(data: Uri?) {
        if (data?.host == "success") {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        // Cuando el usuario regresa del navegador (sea por éxito o cierre manual)
        // Damos un pequeño delay para que Android procese el Deep Link primero.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                // Si la actividad sigue viva aquí, es porque NO se disparó el success
                // pero el usuario ya volvió a la app. Le damos por OK para que pueda
                // terminar su orden (Simulación Sandbox).
                setResult(Activity.RESULT_OK)
                finish()
            }
        }, 1000)
    }
}
