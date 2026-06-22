package com.cibertec.cibertecapp.features.devices.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityDevicesListBinding
import com.cibertec.cibertecapp.databinding.ItemCategoryChipBinding
import com.cibertec.cibertecapp.features.devices.presentation.adapters.DeviceAdapter
import com.cibertec.cibertecapp.features.devices.presentation.viewmodels.DevicesViewModel
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairsActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairDetailActivity
import com.cibertec.cibertecapp.features.profile.presentation.activities.ProfileActivity
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import kotlinx.coroutines.launch

class DevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesListBinding
    private val viewModel: DevicesViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSearch()
        setupCategories()
        setupRecycler()
        setupBottomNavigation()
        observeState()
        
        binding.fabAddDevice.setOnClickListener {
            startActivity(Intent(this, AddDeviceActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_devices
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, com.cibertec.cibertecapp.features.requests.presentation.activities.RequestsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_repairs -> {
                    startActivity(Intent(this, RepairsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_devices -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_devices
        viewModel.loadDevices()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { 
            finish()
        }
    }

    private fun setupCategories() {
        setupChip(binding.catAll, "Todos", R.drawable.ic_list_alt)
        setupChip(binding.catSmartphone, "Smartphone", R.drawable.ic_smartphone)
        setupChip(binding.catLaptop, "Laptop", R.drawable.ic_laptop)
        setupChip(binding.catTablet, "Tablet", R.drawable.ic_devices)
        setupChip(binding.catGaming, "Gaming", R.drawable.ic_gaming)

        binding.catAll.root.setOnClickListener { selectCategory("") }
        binding.catSmartphone.root.setOnClickListener { selectCategory("Smartphone") }
        binding.catLaptop.root.setOnClickListener { selectCategory("Laptop") }
        binding.catTablet.root.setOnClickListener { selectCategory("Tablet") }
        binding.catGaming.root.setOnClickListener { selectCategory("Gaming") }
        
        selectCategory("") 
    }

    private fun setupChip(chipBinding: ItemCategoryChipBinding, name: String, icon: Int) {
        chipBinding.tvCatName.text = name
        chipBinding.ivCatIcon.setImageResource(icon)
    }

    private fun selectCategory(category: String) {
        viewModel.filterByCategory(category)
        
        val chips = listOf(
            "" to binding.catAll,
            "Smartphone" to binding.catSmartphone,
            "Laptop" to binding.catLaptop,
            "Tablet" to binding.catTablet,
            "Gaming" to binding.catGaming
        )

        chips.forEach { (cat, chip) ->
            val isSelected = cat == category
            chip.root.strokeWidth = if (isSelected) 4 else 1 
            chip.root.setStrokeColor(getColor(if (isSelected) R.color.brand_blue else R.color.outline_variant))
            chip.tvCatName.setTextColor(getColor(if (isSelected) R.color.brand_blue else R.color.on_surface_variant))
            chip.ivCatIcon.setColorFilter(getColor(if (isSelected) R.color.brand_blue else R.color.on_surface_variant))
        }
    }

    private fun setupSearch() {
        binding.etSearchDevices.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchDevices(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecycler() {
        deviceAdapter = DeviceAdapter { device, imageView ->
            val intent = Intent(this, DeviceDetailActivity::class.java)
            intent.putExtra("DEVICE_ID", device.id)
            
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, 
                imageView, 
                "device_image_transition"
            )
            startActivity(intent, options.toBundle())
        }
        binding.rvDevices.apply {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(this@DevicesActivity)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isLoading) {
                    binding.shimmerContainer.startShimmer()
                    binding.shimmerContainer.visibility = View.VISIBLE
                    binding.rvDevices.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                } else {
                    binding.shimmerContainer.stopShimmer()
                    binding.shimmerContainer.visibility = View.GONE
                    
                    if (state.devices.isEmpty()) {
                        binding.rvDevices.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvDevices.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        deviceAdapter.updateList(state.devices)
                    }
                }
            }
        }
    }
}
