package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityRepairsListBinding
import com.cibertec.cibertecapp.databinding.ItemCategoryChipBinding
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.cibertec.cibertecapp.features.home.presentation.adapters.RepairAdapter
import com.cibertec.cibertecapp.features.profile.presentation.activities.ProfileActivity
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.RepairsViewModel
import com.cibertec.cibertecapp.features.devices.presentation.activities.DevicesActivity
import kotlinx.coroutines.launch

class RepairsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRepairsListBinding
    private val viewModel: RepairsViewModel by viewModels()
    private lateinit var repairAdapter: RepairAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepairsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSearch()
        setupCategories()
        setupRecycler()
        setupBottomNavigation()
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.fabAddRepair.setOnClickListener {
            val intent = Intent(this, NewRepairActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_repairs
        viewModel.loadRepairs()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { 
            finish()
        }
    }

    private fun setupSearch() {
        binding.etSearchRepairs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchRepairs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    private fun setupRecycler() {
        repairAdapter = RepairAdapter { repair ->
            val intent = Intent(this, RepairDetailActivity::class.java).apply {
                putExtra("REPAIR_ID", repair.id)
            }
            startActivity(intent)
        }
        binding.rvRepairs.apply {
            adapter = repairAdapter
            layoutManager = LinearLayoutManager(this@RepairsActivity)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_repairs
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
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_repairs -> true
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
                    binding.rvRepairs.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                } else {
                    binding.shimmerContainer.stopShimmer()
                    binding.shimmerContainer.visibility = View.GONE
                    
                    if (state.repairs.isEmpty()) {
                        binding.rvRepairs.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvRepairs.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        repairAdapter.updateList(state.repairs)
                    }
                }
            }
        }
    }
}
