package com.cibertec.cibertecapp.features.requests.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityRequestsListBinding
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.cibertec.cibertecapp.features.profile.presentation.activities.ProfileActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.NewRepairActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairsActivity
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import com.cibertec.cibertecapp.features.requests.presentation.adapters.QuotationAdapter
import com.cibertec.cibertecapp.features.requests.presentation.viewmodels.RequestsViewModel
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
        viewModel.loadRequests()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = QuotationAdapter { req ->
            handleRequestClick(req)
        }
        binding.rvRequests.apply {
            adapter = this@RequestsActivity.adapter
            layoutManager = LinearLayoutManager(this@RequestsActivity)
        }
    }

    private fun handleRequestClick(req: QuotationRequest) {
        if (req.status.uppercase() == "COTIZADO") {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cotización Recibida")
                .setMessage("¿Qué deseas hacer con esta solicitud?")
                .setPositiveButton("Aceptar y Pagar") { _, _ ->
                    val intent = Intent(this, NewRepairActivity::class.java).apply {
                        putExtra("FROM_QUOTATION", true)
                        putExtra("QUOTATION_DATA", req)
                    }
                    startActivity(intent)
                }
                .setNeutralButton("Cancelar Solicitud") { _, _ ->
                    showConfirmCancelDialog(req.id)
                }
                .setNegativeButton("Cerrar", null)
                .show()
        } else {
            showRequestDetailsDialog(req)
        }
    }

    private fun showRequestDetailsDialog(req: QuotationRequest) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Detalles de Solicitud")
            .setMessage("""
                ID: #REQ-${req.id.takeLast(5).uppercase()}
                Equipo: ${req.brandAndModel}
                Estado: ${req.status}
                
                Descripción:
                ${req.problemDescription}
                
                ${if (req.adminComment.isNotEmpty()) "\nRespuesta del técnico:\n${req.adminComment}" else "\nEstamos procesando tu solicitud. Te notificaremos cuando tengamos un presupuesto listo."}
            """.trimIndent())
            .setPositiveButton("Entendido", null)
            .setNegativeButton("Cancelar Solicitud") { _, _ ->
                showConfirmCancelDialog(req.id)
            }
            .show()
    }

    private fun showConfirmCancelDialog(requestId: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar solicitud?")
            .setMessage("Esta acción cancelará tu pedido de cotización permanentemente.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteRequest(requestId)
                Toast.makeText(this, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
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
