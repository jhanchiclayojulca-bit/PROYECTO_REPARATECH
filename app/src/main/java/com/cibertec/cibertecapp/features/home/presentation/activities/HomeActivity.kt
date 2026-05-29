package com.cibertec.cibertecapp.features.home.presentation.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cibertec.cibertecapp.databinding.ActivityHomeClientBinding
import com.cibertec.cibertecapp.features.home.presentation.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.cibertec.cibertecapp.R
import com.google.android.material.progressindicator.LinearProgressIndicator

class HomeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHomeClientBinding

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeClientBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupListeners()

        observeState()

    }

    private fun observeState() {

        lifecycleScope.launch {

            viewModel.state.collect { state ->

                binding.repairsContainer.removeAllViews()

                state.repairs.forEach { repair ->

                    val repairView = LayoutInflater.from(this@HomeActivity)
                        .inflate(
                            R.layout.item_repair_card,
                            binding.repairsContainer,
                            false
                        )

                    val tvRepairName =
                        repairView.findViewById<TextView>(R.id.tvRepairName)

                    val tvRepairId =
                        repairView.findViewById<TextView>(R.id.tvRepairId)

                    val tvRepairStatus =
                        repairView.findViewById<TextView>(R.id.tvRepairStatus)

                    val tvDeliveryDate =
                        repairView.findViewById<TextView>(R.id.tvDeliveryDate)

                    val repairProgress =
                        repairView.findViewById<LinearProgressIndicator>(R.id.repairProgress)

                    val ivRepairIcon =
                        repairView.findViewById<ImageView>(R.id.ivRepairIcon)

                    tvRepairName.text = repair.deviceName

                    tvRepairId.text =
                        "${repair.orderId} • ${repair.status}"

                    tvRepairStatus.text =
                        repair.status.uppercase()

                    tvDeliveryDate.text =
                        "Entrega estimada: ${repair.date}"

                    repairProgress.progress =
                        repair.progress

                    ivRepairIcon.setImageResource(
                        repair.iconRes
                    )

                    binding.repairsContainer.addView(
                        repairView
                    )
                }
            }
        }
    }
    private fun setupListeners() {

        binding.catLaptop.setOnClickListener {

            viewModel.filterRepairs("Laptops")
        }

        binding.catCelulares.setOnClickListener {

            viewModel.filterRepairs("Celulares")
        }

        binding.catGaming.setOnClickListener {

            viewModel.filterRepairs("Gaming")
        }

        binding.catTablets.setOnClickListener {

            viewModel.filterRepairs("Tablets")
        }

        binding.btnViewAll.setOnClickListener {

            viewModel.showAllRepairs()
        }
    }

}