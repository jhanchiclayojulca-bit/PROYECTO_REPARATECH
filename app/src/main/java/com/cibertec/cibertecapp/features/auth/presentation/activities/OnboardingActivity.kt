package com.cibertec.cibertecapp.features.auth.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupListeners()
    }

    private fun setupViewPager() {
        val pages = listOf(
            OnboardingPage(
                "Tus Equipos a Salvo",
                "Registra tus dispositivos y lleva un inventario detallado de su estado y reparaciones.",
                R.drawable.ic_devices
            ),
            OnboardingPage(
                "Cotizaciones al Instante",
                "Solicita presupuestos para tus averías y recibe respuestas rápidas de nuestros expertos.",
                R.drawable.ic_list_alt
            ),
            OnboardingPage(
                "Soporte Premium 24/7",
                "Gestiona pagos, consulta el progreso en tiempo real y obtén ayuda personalizada.",
                R.drawable.ic_help
            )
        )

        val adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pages.size - 1) {
                    binding.btnAction.text = "Empezar ahora"
                    binding.btnSkip.visibility = View.INVISIBLE
                } else {
                    binding.btnAction.text = "Siguiente"
                    binding.btnSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnAction.setOnClickListener {
            if (binding.viewPager.currentItem < 2) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // Guardar que ya vio el onboarding
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_finished", true).apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    data class OnboardingPage(val title: String, val desc: String, val imageRes: Int)

    inner class OnboardingAdapter(private val pages: List<OnboardingPage>) : 
        RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvOnboardingTitle)
            val desc: TextView = view.findViewById(R.id.tvOnboardingDesc)
            val image: ImageView = view.findViewById(R.id.ivOnboardingImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val page = pages[position]
            holder.title.text = page.title
            holder.desc.text = page.desc
            holder.image.setImageResource(page.imageRes)
        }

        override fun getItemCount() = pages.size
    }
}
