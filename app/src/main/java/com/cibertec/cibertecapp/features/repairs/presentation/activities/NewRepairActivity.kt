package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityNewRepairContainerBinding
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.NewRepairViewModel
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class NewRepairActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewRepairContainerBinding
    private val viewModel: NewRepairViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRepairContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isQuotation = intent.getBooleanExtra("IS_QUOTATION_ONLY", false)
        viewModel.isQuotationOnly = isQuotation
        
        if (isQuotation) {
            binding.topAppBar.title = "Nueva Solicitud"
            binding.tvStepNumber.visibility = View.GONE
            binding.progressStep2.visibility = View.GONE
            binding.progressStep3.visibility = View.GONE
        }

        val fromQuotation = intent.getBooleanExtra("FROM_QUOTATION", false)
        if (fromQuotation) {
            val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("QUOTATION_DATA", QuotationRequest::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("QUOTATION_DATA")
            }
            data?.let { viewModel.loadFromQuotation(it) }
        }

        setupToolbar()
        observeCurrentStep()
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Cancelar solicitud?")
            .setMessage("Si sales ahora, perderás todos los datos ingresados.")
            .setPositiveButton("Sí, salir") { _, _ -> finish() }
            .setNegativeButton("Continuar", null)
            .show()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeCurrentStep() {
        lifecycleScope.launch {
            viewModel.currentStep.collect { step ->
                if (!viewModel.isQuotationOnly) {
                    updateStepUI(step)
                }
            }
        }
    }

    private fun updateStepUI(step: Int) {
        val activeColor = getColor(R.color.brand_blue)
        val inactiveColor = getColor(R.color.outline_variant)

        binding.tvStepNumber.text = "Paso $step de 3"
        
        when(step) {
            1 -> {
                binding.tvStepTitle.text = "Detalles del equipo"
                binding.progressStep1.setBackgroundColor(activeColor)
                binding.progressStep2.setBackgroundColor(inactiveColor)
                binding.progressStep3.setBackgroundColor(inactiveColor)
            }
            2 -> {
                binding.tvStepTitle.text = "Configuración"
                binding.progressStep1.setBackgroundColor(activeColor)
                binding.progressStep2.setBackgroundColor(activeColor)
                binding.progressStep3.setBackgroundColor(inactiveColor)
            }
            3 -> {
                binding.tvStepTitle.text = "Resumen"
                binding.progressStep1.setBackgroundColor(activeColor)
                binding.progressStep2.setBackgroundColor(activeColor)
                binding.progressStep3.setBackgroundColor(activeColor)
            }
        }
    }
}
