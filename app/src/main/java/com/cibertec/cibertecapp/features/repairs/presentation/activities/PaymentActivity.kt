package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent

class PaymentActivity : AppCompatActivity() {

    private var browserLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (intent?.data?.scheme == "reparatech") {
            handleResult(intent.data)
            return
        }

        val url = intent.getStringExtra("PAYMENT_URL") ?: ""
        if (url.isNotEmpty() && !browserLaunched) {
            browserLaunched = true
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(this, Uri.parse(url))
        } else {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.data?.scheme == "reparatech") {
            handleResult(intent.data)
        }
    }

    private fun handleResult(data: Uri?) {
        val resultIntent = Intent()
        if (data?.host == "success") {
            // Capturamos el payment_id real que devuelve Mercado Pago en la URL
            val paymentId = data.getQueryParameter("payment_id") ?: "N/A"
            resultIntent.putExtra("PAYMENT_ID", paymentId)
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        if (browserLaunched) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }, 500)
        }
    }
}
