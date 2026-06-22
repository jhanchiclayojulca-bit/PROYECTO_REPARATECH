package com.cibertec.cibertecapp.features.tips.presentation.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.cibertecapp.databinding.ActivityTipsBinding

class TipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
