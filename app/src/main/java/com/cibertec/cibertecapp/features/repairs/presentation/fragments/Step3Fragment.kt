package com.cibertec.cibertecapp.features.repairs.presentation.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.FragmentNewRepairStep3Binding
import com.cibertec.cibertecapp.features.repairs.presentation.activities.PaymentActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.PaymentReceiptActivity
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.NewRepairViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class Step3Fragment : Fragment() {

    private var _binding: FragmentNewRepairStep3Binding? = null
    private val binding get() = _binding!!
    private val viewModel: NewRepairViewModel by activityViewModels()
    private var selectedMethod = "Tarjeta"
    private var isNavigating = false
    private var verificationDialog: androidx.appcompat.app.AlertDialog? = null

    private val paymentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val paymentId = result.data?.getStringExtra("PAYMENT_ID") ?: "PAG-${(1000..9999).random()}"

            // MOSTRAR ALERTA DE VERIFICACIÓN
            verificationDialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Verificando Pago")
                .setMessage("Estamos validando tu transacción con Mercado Pago. ID: $paymentId")
                .setCancelable(false)
                .show()

            lifecycleScope.launch {
                viewModel.updatePaymentDetails(paymentId)
                delay(1500) // Breve pausa estética
                showProcessingState()
                viewModel.confirmOrder()
            }
        } else {
            Toast.makeText(requireContext(), "Pago no completado", Toast.LENGTH_SHORT).show()
        }
    }

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

    private fun showProcessingState() {
        // Bloqueamos el botón y cambiamos el texto como indicador de carga
        binding.btnConfirmPay.isEnabled = false
        binding.btnConfirmPay.text = "Confirmando..."
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
                binding.tvTotalCost.text = String.format(Locale.getDefault(), "S/.%.2f", req.total)

                val imageToLoad: Any = when {
                    req.photoUrl.isNotEmpty() -> req.photoUrl
                    viewModel.selectedImageUri.value != null -> viewModel.selectedImageUri.value!!
                    else -> R.mipmap.ic_launcher
                }
                
                binding.ivSummaryDevice.load(imageToLoad) {
                    crossfade(true)
                    placeholder(R.drawable.ic_laptop)
                }

                if (!state.isSuccess) {
                    binding.btnConfirmPay.isEnabled = binding.cbTerms.isChecked && !state.isLoading
                    binding.btnConfirmPay.text = if (state.isLoading) "Finalizando..." else "Confirmar y Pagar"
                }

                if (state.isSuccess && !isNavigating) {
                    isNavigating = true
                    verificationDialog?.dismiss() // Cerramos la alerta JUSTO antes de pasar a la siguiente pantalla
                    delay(800) // Pausa mínima para que Firebase guarde bien
                    val intent = Intent(requireContext(), PaymentReceiptActivity::class.java).apply {
                        putExtra("REPAIR_REQUEST", req)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.cardPayCard.setOnClickListener { selectPayment("Tarjeta") }
        binding.cardPayStore.setOnClickListener { selectPayment("Tienda") }

        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnConfirmPay.isEnabled = isChecked && !viewModel.state.value.isLoading
        }

        binding.btnConfirmPay.setOnClickListener {
            if (selectedMethod == "Tarjeta") {
                launchMercadoPago()
            } else {
                viewModel.confirmOrder()
            }
        }
    }

    private fun launchMercadoPago() {
        val totalAmount = viewModel.state.value.request.total
        val accessToken = getString(R.string.mercadopago_access_token)

        lifecycleScope.launch {
            try {
                val url = createPaymentPreference(totalAmount, accessToken)
                url?.let {
                    val intent = Intent(requireContext(), PaymentActivity::class.java).apply {
                        putExtra("PAYMENT_URL", it)
                    }
                    paymentLauncher.launch(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun createPaymentPreference(amount: Double, token: String): String? {
        val client = okhttp3.OkHttpClient()
        val json = """
            {
                "items": [{"title": "ReparaTech", "quantity": 1, "unit_price": $amount, "currency_id": "PEN"}],
                "back_urls": {
                    "success": "reparatech://success",
                    "failure": "reparatech://failure",
                    "pending": "reparatech://success"
                },
                "auto_return": "all"
            }
        """.trimIndent()
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = okhttp3.Request.Builder()
            .url("https://api.mercadopago.com/checkout/preferences")
            .header("Authorization", "Bearer $token")
            .post(body).build()

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                org.json.JSONObject(response.body?.string() ?: "").getString("init_point")
            } else null
        }
    }

    private fun selectPayment(method: String) {
        selectedMethod = method
        viewModel.updatePaymentMethod(method)
        binding.rbPayCard.isChecked = (method == "Tarjeta")
        binding.rbPayStore.isChecked = (method == "Tienda")
        updateCardStroke(binding.cardPayCard, (method == "Tarjeta"))
        updateCardStroke(binding.cardPayStore, (method == "Tienda"))
    }

    private fun updateCardStroke(card: com.google.android.material.card.MaterialCardView, isSelected: Boolean) {
        card.strokeWidth = if (isSelected) 6 else 2
        card.setStrokeColor(requireContext().getColor(if (isSelected) R.color.brand_blue else R.color.outline_variant))
    }
}
