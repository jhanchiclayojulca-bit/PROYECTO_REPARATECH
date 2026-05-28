package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityRequestConfirmationBinding
import kotlin.random.Random

class RequestConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID de orden real generado
        val orderNum = intent.getStringExtra("ORDER_ID") ?: "ORD-0000X"
        binding.tvOrderNumber.text = orderNum

        binding.btnBackHome.setOnClickListener {
            val intent = Intent(this, HomeClientActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}
