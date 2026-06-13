package com.cibertec.cibertecapp.features.repairs.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.FragmentNewRepairStep2Binding
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.NewRepairViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class Step2Fragment : Fragment() {

    private var _binding: FragmentNewRepairStep2Binding? = null
    private val binding get() = _binding!!
    private val viewModel: NewRepairViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewRepairStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateStep(2)
        setupListeners()
        restoreSelections()
    }

    private fun setupListeners() {
        binding.cardStandard.setOnClickListener { selectService(false) }
        binding.cardExpress.setOnClickListener { selectService(true) }
        
        binding.cardPresencial.setOnClickListener { 
            selectDelivery("Presencial") 
            showStoreAddressInfo()
        }
        
        binding.cardRecogida.setOnClickListener { 
            checkAddressAndSelectDelivery()
        }

        binding.btnNextStep2.setOnClickListener {
            findNavController().navigate(R.id.action_step2_to_step3)
        }
    }

    private fun showStoreAddressInfo() {
        Toast.makeText(requireContext(), "Encuentra nuestra direccion en la sección de Centro de Ayuda.", Toast.LENGTH_LONG).show()
    }

    private fun checkAddressAndSelectDelivery() {
        viewLifecycleOwner.lifecycleScope.launch {
            val address = viewModel.getUserAddress()
            if (address.isEmpty() || address == "No registrada") {
                showAddressInputDialog()
            } else {
                selectDelivery("Recogida")
                Toast.makeText(requireContext(), "Se usará tu dirección registrada: $address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddressInputDialog() {
        val input = android.widget.EditText(requireContext())
        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(60, 20, 60, 20)
        input.layoutParams = params
        input.hint = "Ingresa tu dirección completa"
        container.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Dirección requerida")
            .setMessage("Para el recojo a domicilio, necesitamos saber tu dirección. Esta se guardará en tu perfil.")
            .setView(container)
            .setPositiveButton("Guardar y Continuar") { _, _ ->
                val newAddress = input.text.toString().trim()
                if (newAddress.isNotEmpty()) {
                    viewModel.updateAddress(newAddress)
                    selectDelivery("Recogida")
                    Toast.makeText(requireContext(), "Dirección guardada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Debes ingresar una dirección", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun selectService(isExpress: Boolean) {
        viewModel.updateServiceType(isExpress)
        binding.rbStandard.isChecked = !isExpress
        binding.rbExpress.isChecked = isExpress
        
        binding.cardExpress.setStrokeWidth(if (isExpress) 6 else 2)
        binding.cardExpress.setStrokeColor(requireContext().getColor(if (isExpress) R.color.brand_blue else R.color.outline_variant))
        
        binding.cardStandard.setStrokeWidth(if (!isExpress) 6 else 2)
        binding.cardStandard.setStrokeColor(requireContext().getColor(if (!isExpress) R.color.brand_blue else R.color.outline_variant))
    }

    private fun selectDelivery(method: String) {
        viewModel.updateDeliveryMethod(method)
        val isHome = method == "Recogida"
        
        binding.cardRecogida.setStrokeWidth(if (isHome) 6 else 2)
        binding.cardRecogida.setStrokeColor(requireContext().getColor(if (isHome) R.color.brand_blue else R.color.outline_variant))
        
        binding.cardPresencial.setStrokeWidth(if (!isHome) 6 else 2)
        binding.cardPresencial.setStrokeColor(requireContext().getColor(if (!isHome) R.color.brand_blue else R.color.outline_variant))
    }

    private fun restoreSelections() {
        val req = viewModel.state.value.request
        selectService(req.serviceType == "Express")
        if (req.deliveryMethod.isNotEmpty()) {
            selectDelivery(req.deliveryMethod)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
