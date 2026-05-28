package com.cibertec.cibertecapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityPaymentReceiptBinding

class PaymentReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentReceiptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val amount = intent.getDoubleExtra("AMOUNT", 100.0)
        val orderId = intent.getStringExtra("ORDER_ID") ?: "ORD-0000"

        binding.tvReceiptAmount.text = "S/ ${String.format("%.2f", amount)}"
        binding.tvReceiptOrderId.text = orderId
        binding.tvReceiptMessage.text = "Tu pago de S/ ${String.format("%.2f", amount)} ha sido procesado"

        binding.btnGoToRepairs.setOnClickListener {
            val intent = Intent(this, RepairsClientActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
