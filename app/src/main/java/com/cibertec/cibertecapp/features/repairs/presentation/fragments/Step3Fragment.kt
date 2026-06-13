package com.cibertec.cibertecapp.features.repairs.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.FragmentNewRepairStep3Binding
import com.cibertec.cibertecapp.features.repairs.presentation.activities.PaymentReceiptActivity
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.NewRepairViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class Step3Fragment : Fragment() {

    private var _binding: FragmentNewRepairStep3Binding? = null
    private val binding get() = _binding!!
    private val viewModel: NewRepairViewModel by activityViewModels()
    private var selectedMethod = "Tarjeta"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewRepairStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateStep(3)
        observeState()
        setupListeners()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val req = state.request
                binding.tvSummaryModel.text = req.brandAndModel
                binding.tvSummaryProblem.text = req.problemDescription
                binding.tvSummaryService.text = "Reparación ${req.serviceType}"
                binding.tvSummaryLogistics.text = if (req.deliveryMethod == "Recogida") "Recogida a domicilio" else "Entrega en tienda"
                
                binding.tvBaseCost.text = String.format(Locale.getDefault(), "S/.%.2f", req.baseCost + req.tax)
                binding.tvAdditionalCost.text = String.format(Locale.getDefault(), "S/.%.2f", req.additionalCost)
                binding.tvTotalCost.text = String.format(Locale.getDefault(), "S/.%.0f", req.total)

                viewModel.selectedImageUri.value?.let { uri ->
                    binding.ivSummaryDevice.load(uri) { crossfade(true) }
                }

                binding.btnConfirmPay.isEnabled = !state.isLoading && binding.cbTerms.isChecked
                binding.btnConfirmPay.text = if (state.isLoading) "Procesando..." else "Confirmar y Pagar"

                if (state.isSuccess) {
                    val intent = Intent(requireContext(), PaymentReceiptActivity::class.java).apply {
                        putExtra("REPAIR_REQUEST", req)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.cardPayCard.setOnClickListener { selectPayment("Tarjeta") }
        binding.cardPayStore.setOnClickListener { selectPayment("Tienda") }
        binding.cardPayBank.setOnClickListener { selectPayment("Depósito") }

        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnConfirmPay.isEnabled = isChecked && !viewModel.state.value.isLoading
        }

        binding.btnConfirmPay.setOnClickListener {
            when (selectedMethod) {
                "Tarjeta" -> showCardDialog()
                "Depósito" -> showBankInfoDialog()
                else -> viewModel.confirmOrder()
            }
        }
    }

    private fun selectPayment(method: String) {
        selectedMethod = method
        viewModel.updatePaymentMethod(method)
        
        binding.rbPayCard.isChecked = method == "Tarjeta"
        binding.rbPayStore.isChecked = method == "Tienda"
        binding.rbPayBank.isChecked = method == "Depósito"

        updateCardStroke(binding.cardPayCard, method == "Tarjeta")
        updateCardStroke(binding.cardPayStore, method == "Tienda")
        updateCardStroke(binding.cardPayBank, method == "Depósito")
    }

    private fun updateCardStroke(card: com.google.android.material.card.MaterialCardView, isSelected: Boolean) {
        card.strokeWidth = if (isSelected) 6 else 2
        card.setStrokeColor(requireContext().getColor(if (isSelected) R.color.brand_blue else R.color.outline_variant))
    }

    private fun showCardDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_form, null)
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Pagar Ahora") { _, _ -> processSimulatedPayment("Tarjeta") }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showBankInfoDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Información de Depósito")
            .setMessage("Cuenta BCP: 191-23456789-0-12\nCCI: 002-191-0023456789012-54\nTitular: ReparaTech S.A.C.\n\nPor favor, realiza la transferencia y dale a confirmar.")
            .setPositiveButton("Ya deposité") { _, _ -> processSimulatedPayment("Depósito") }
            .setNegativeButton("Atrás", null)
            .show()
    }

    private fun processSimulatedPayment(method: String) {
        lifecycleScope.launch {
            val message = if (method == "Tarjeta") "Validando tarjeta..." else "Verificando depósito..."
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Procesando")
                .setMessage(message)
                .setCancelable(false)
                .show().also { dialog ->
                    delay(2000)
                    dialog.dismiss()
                    viewModel.confirmOrder()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
