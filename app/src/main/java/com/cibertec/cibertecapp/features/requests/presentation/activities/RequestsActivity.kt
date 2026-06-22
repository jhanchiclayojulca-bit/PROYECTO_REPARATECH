package com.cibertec.cibertecapp.features.requests.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityRequestsListBinding
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.cibertec.cibertecapp.features.profile.presentation.activities.ProfileActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.NewRepairActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairsActivity
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import com.cibertec.cibertecapp.features.requests.presentation.adapters.QuotationAdapter
import com.cibertec.cibertecapp.features.requests.presentation.viewmodels.RequestsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsListBinding
    private val viewModel: RequestsViewModel by viewModels()
    private lateinit var adapter: QuotationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        setupBottomNavigation()
        observeState()
        
        binding.fabAddRequest.setOnClickListener {
            val intent = Intent(this, NewRepairActivity::class.java).apply {
                putExtra("IS_QUOTATION_ONLY", true)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_requests
        viewModel.loadRequests()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = QuotationAdapter(
            onClick = { req -> handleRequestClick(req) },
            onRejectClick = { req -> showConfirmCancelDialog(req.id) }
        )
        binding.rvRequests.apply {
            adapter = this@RequestsActivity.adapter
            layoutManager = LinearLayoutManager(this@RequestsActivity)
        }
    }

    private fun handleRequestClick(req: QuotationRequest) {
        if (req.status.uppercase() == "COTIZADO") {
            // Si está cotizado, al dar clic en la tarjeta o botón principal (Aceptar)
            // podemos o mostrar el detalle o ir directo. Por consistencia, mostramos el detalle
            // donde ya tiene los botones configurados.
            showRequestDetailsDialog(req)
        } else {
            showRequestDetailsDialog(req)
        }
    }

    private fun showRequestDetailsDialog(req: QuotationRequest) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_request_details, null)
        
        val ivIcon = dialogView.findViewById<ImageView>(R.id.ivStatusIcon)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatusTitle)
        val tvId = dialogView.findViewById<TextView>(R.id.tvRequestId)
        val ivThumb = dialogView.findViewById<ImageView>(R.id.ivDeviceThumb)
        val tvName = dialogView.findViewById<TextView>(R.id.tvDeviceName)
        val tvSerial = dialogView.findViewById<TextView>(R.id.tvDeviceSerial)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvProblemDesc)
        val layoutAdmin = dialogView.findViewById<View>(R.id.layoutAdminResp)
        val tvAdmin = dialogView.findViewById<TextView>(R.id.tvAdminComment)
        val layoutPrice = dialogView.findViewById<View>(R.id.layoutPrice)
        val tvPrice = dialogView.findViewById<TextView>(R.id.tvPriceValue)
        val tvFootnote = dialogView.findViewById<TextView>(R.id.tvFootnote)
        
        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btnDialogPositive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btnDialogNegative)

        tvId.text = "#REQ-${req.id.takeLast(5).uppercase()}"
        tvName.text = req.brandAndModel
        tvSerial.text = "S/N: ${req.serialNumber}"
        tvDesc.text = req.problemDescription
        ivThumb.load(req.photoUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_laptop)
            error(R.drawable.ic_laptop)
        }

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        when (req.status.uppercase()) {
            "COTIZADO" -> {
                ivIcon.setImageResource(R.drawable.ic_check)
                ivIcon.setColorFilter(getColor(R.color.status_green_text))
                tvStatus.text = "¡Presupuesto Listo!"
                tvStatus.setTextColor(getColor(R.color.status_green_text))
                
                tvFootnote.visibility = View.GONE
                layoutPrice.visibility = View.VISIBLE
                tvPrice.text = String.format(java.util.Locale.getDefault(), "S/. %.2f", req.estimatedPrice)

                if (req.adminComment.isNotEmpty()) {
                    layoutAdmin.visibility = View.VISIBLE
                    tvAdmin.text = req.adminComment
                }

                btnPositive.text = "Aceptar y Pagar"
                btnPositive.setOnClickListener {
                    alertDialog.dismiss()
                    val intent = Intent(this, NewRepairActivity::class.java).apply {
                        putExtra("FROM_QUOTATION", true)
                        putExtra("QUOTATION_DATA", req)
                    }
                    startActivity(intent)
                }
                
                btnNegative.text = "Rechazar"
                btnNegative.setOnClickListener {
                    alertDialog.dismiss()
                    showConfirmCancelDialog(req.id)
                }
            }
            "RECHAZADO" -> {
                ivIcon.setImageResource(R.drawable.ic_delete)
                ivIcon.setColorFilter(getColor(R.color.status_orange_text))
                tvStatus.text = "Solicitud Rechazada"
                tvStatus.setTextColor(getColor(R.color.status_orange_text))
                
                if (req.adminComment.isNotEmpty()) {
                    layoutAdmin.visibility = View.VISIBLE
                    tvAdmin.text = req.adminComment
                }
                
                btnPositive.text = "Entendido"
                btnPositive.setOnClickListener { alertDialog.dismiss() }
                
                btnNegative.text = "Eliminar"
                btnNegative.setOnClickListener {
                    alertDialog.dismiss()
                    showConfirmCancelDialog(req.id)
                }
            }
            else -> {
                btnPositive.text = "Entendido"
                btnPositive.setOnClickListener { alertDialog.dismiss() }
                
                btnNegative.text = "Cancelar"
                btnNegative.setOnClickListener {
                    alertDialog.dismiss()
                    showConfirmCancelDialog(req.id)
                }
            }
        }
        alertDialog.show()
    }

    private fun showConfirmCancelDialog(requestId: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Confirmar acción?")
            .setMessage("Esta acción eliminará la solicitud de forma permanente.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteRequest(requestId)
                Toast.makeText(this, "Solicitud eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_requests
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_requests -> true
                R.id.nav_repairs -> {
                    startActivity(Intent(this, RepairsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, com.cibertec.cibertecapp.features.devices.presentation.activities.DevicesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isLoading) {
                    binding.shimmerContainer.startShimmer()
                    binding.shimmerContainer.visibility = View.VISIBLE
                    binding.rvRequests.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                } else {
                    binding.shimmerContainer.stopShimmer()
                    binding.shimmerContainer.visibility = View.GONE
                    
                    if (state.requests.isEmpty()) {
                        binding.rvRequests.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvRequests.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        adapter.updateList(state.requests)
                    }
                }
            }
        }
    }
}
