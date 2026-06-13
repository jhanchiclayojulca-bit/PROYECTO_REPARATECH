package com.cibertec.cibertecapp.features.home.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityHomeClientBinding
import com.cibertec.cibertecapp.features.home.presentation.viewmodels.HomeViewModel
import com.cibertec.cibertecapp.features.home.presentation.adapters.RepairAdapter
import com.cibertec.cibertecapp.features.repairs.presentation.activities.NewRepairActivity
import com.cibertec.cibertecapp.features.profile.presentation.activities.ProfileActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairDetailActivity
import com.cibertec.cibertecapp.features.devices.presentation.activities.DevicesActivity
import com.cibertec.cibertecapp.features.repairs.presentation.activities.RepairsActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeClientBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var repairAdapter: RepairAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupSearch()
        setupRecycler()
        setupBottomNavigation()
        observeState()
        observeSelectedCategory()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRepairs()
    }

    private fun setupRecycler() {
        repairAdapter = RepairAdapter { repair ->
            val intent = Intent(this, RepairDetailActivity::class.java)
            intent.putExtra("REPAIR_ID", repair.id)
            startActivity(intent)
        }
        binding.rvRepairs.apply {
            adapter = repairAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isLoading) {
                    binding.shimmerViewContainer.startShimmer()
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                    binding.rvRepairs.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                } else {
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.rvRepairs.visibility = View.VISIBLE

                    repairAdapter.updateList(state.repairs)
                    binding.tvEmpty.visibility = if (state.repairs.isEmpty()) View.VISIBLE else View.GONE
                    
                    // Actualizar UI con datos de usuario
                    binding.tvUserWelcome.text = "Hola, ${state.userName.split(" ").firstOrNull() ?: "Usuario"}"
                    
                    binding.avatarImage.load(state.userPhotoUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_person)
                        error(R.drawable.ic_person)
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.avatarImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.catLaptop.setOnClickListener { viewModel.filterRepairs("Laptop") }
        binding.catCelulares.setOnClickListener { viewModel.filterRepairs("Smartphone") }
        binding.catGaming.setOnClickListener { viewModel.filterRepairs("Gaming") }
        binding.catTablets.setOnClickListener { viewModel.filterRepairs("Tablet") }

        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(this, RepairsActivity::class.java))
        }

        binding.btnNewRequest.setOnClickListener {
            startActivity(Intent(this, NewRepairActivity::class.java))
        }

        binding.btnMyDevices.setOnClickListener {
            startActivity(Intent(this, DevicesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro que deseas salir?")
            .setPositiveButton("Salir") { _, _ ->
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, com.cibertec.cibertecapp.features.auth.presentation.activities.LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_requests -> {
                    startActivity(Intent(this, com.cibertec.cibertecapp.features.requests.presentation.activities.RequestsActivity::class.java))
                    true
                }
                R.id.nav_repairs -> {
                    startActivity(Intent(this, RepairsActivity::class.java))
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DevicesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun observeSelectedCategory() {
        lifecycleScope.launch {
            viewModel.selectedCategory.collect { category ->
                resetCategories()
                when(category) {
                    "Laptop" -> activateCategory(binding.catLaptop)
                    "Smartphone" -> activateCategory(binding.catCelulares)
                    "Tablet" -> activateCategory(binding.catTablets)
                    "Gaming" -> activateCategory(binding.catGaming)
                }
            }
        }
    }

    private fun resetCategories() {
        deactivateCategory(binding.catLaptop)
        deactivateCategory(binding.catCelulares)
        deactivateCategory(binding.catGaming)
        deactivateCategory(binding.catTablets)
    }

    private fun activateCategory(view: LinearLayout) {
        val card = view.getChildAt(0) as com.google.android.material.card.MaterialCardView
        val icon = card.getChildAt(0) as ImageView
        val text = view.getChildAt(1) as TextView

        card.setCardBackgroundColor(getColor(R.color.brand_blue_light))
        icon.setColorFilter(getColor(R.color.brand_blue))
        text.setTextColor(getColor(R.color.on_surface))
        text.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    private fun deactivateCategory(view: LinearLayout) {
        val card = view.getChildAt(0) as com.google.android.material.card.MaterialCardView
        val icon = card.getChildAt(0) as ImageView
        val text = view.getChildAt(1) as TextView

        card.setCardBackgroundColor(getColor(R.color.surface_container))
        icon.setColorFilter(getColor(R.color.on_surface_variant))
        text.setTextColor(getColor(R.color.on_surface_variant))
        text.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchRepairs(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
