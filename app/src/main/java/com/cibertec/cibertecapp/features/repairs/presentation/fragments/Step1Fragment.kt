package com.cibertec.cibertecapp.features.repairs.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.FragmentNewRepairStep1Binding
import com.cibertec.cibertecapp.features.devices.domain.model.Device
import com.cibertec.cibertecapp.features.devices.presentation.adapters.DeviceMiniAdapter
import com.cibertec.cibertecapp.features.repairs.presentation.viewmodels.NewRepairViewModel
import kotlinx.coroutines.launch

class Step1Fragment : Fragment() {

    private var _binding: FragmentNewRepairStep1Binding? = null
    private val binding get() = _binding!!
    private val viewModel: NewRepairViewModel by activityViewModels()
    private lateinit var deviceAdapter: DeviceMiniAdapter
    private var isCategorySelected = false
    private var isUsingStoredDevice = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateImageUri(it)
            showImagePreview(it)
            validateFields()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewRepairStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // SALTO SEGURO AL PASO 2 SI VIENE DE COTIZACIÓN
        if (viewModel.shouldStartAtStep2) {
            viewModel.shouldStartAtStep2 = false // Consumir la bandera
            findNavController().navigate(R.id.action_step1_to_step2)
            return
        }

        viewModel.updateStep(1)
        setupRecycler()
        setupListeners()
        setupValidation()
        observeViewModel()
        restoreImagePreview()
        validateFields()
    }

    private fun setupRecycler() {
        deviceAdapter = DeviceMiniAdapter { device ->
            applySelectedDevice(device)
        }
        binding.rvMyDevices.adapter = deviceAdapter
    }

    private fun applySelectedDevice(device: Device) {
        isUsingStoredDevice = true
        isCategorySelected = true
        viewModel.selectDevice(device)
        
        binding.layoutManualEntry.visibility = View.GONE
        binding.layoutEvidence.visibility = View.GONE 
        binding.cardSelectedDevice.visibility = View.VISIBLE
        
        binding.tvSelectedName.text = "${device.brand} ${device.model}"
        binding.tvSelectedSerial.text = "S/N: ${device.serialNumber}"
        binding.ivSelectedThumb.load(device.photoUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_laptop)
        }
        
        validateFields()
    }

    private fun clearDeviceSelection() {
        isUsingStoredDevice = false
        isCategorySelected = false
        viewModel.updateCategory("")
        binding.layoutManualEntry.visibility = View.VISIBLE
        binding.layoutEvidence.visibility = View.VISIBLE 
        binding.cardSelectedDevice.visibility = View.GONE
        binding.etBrandModel.text = null
        binding.etSerial.text = null
        resetCategoryCards()
        validateFields()
    }

    private fun setupListeners() {
        binding.catSmartphone.setOnClickListener { selectCategory("Smartphone", binding.catSmartphone) }
        binding.catLaptop.setOnClickListener { selectCategory("Laptop", binding.catLaptop) }
        binding.catTablet.setOnClickListener { selectCategory("Tablet", binding.catTablet) }
        binding.catOthers.setOnClickListener { selectCategory("Gaming", binding.catOthers) }

        binding.btnClearSelection.setOnClickListener { clearDeviceSelection() }
        binding.btnUploadPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnRemovePhoto.setOnClickListener {
            viewModel.updateImageUri(null)
            binding.ivPreview.visibility = View.GONE
            binding.layoutEmptyPhoto.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.GONE
            validateFields()
        }

        binding.btnNextStep1.setOnClickListener {
            if (!isUsingStoredDevice) {
                viewModel.updateStep1Details(
                    binding.etBrandModel.text.toString(),
                    binding.etSerial.text.toString(),
                    binding.etDescription.text.toString()
                )
            } else {
                val currentReq = viewModel.state.value.request
                viewModel.updateStep1Details(
                    currentReq.brandAndModel,
                    currentReq.serialNumber,
                    binding.etDescription.text.toString()
                )
            }

            if (viewModel.isQuotationOnly) {
                viewModel.submitQuotation()
            } else {
                findNavController().navigate(R.id.action_step1_to_step2)
            }
        }
    }

    private fun setupValidation() {
        binding.etBrandModel.addTextChangedListener { validateFields() }
        binding.etSerial.addTextChangedListener { validateFields() }
        binding.etDescription.addTextChangedListener { validateFields() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.myDevices.collect { devices ->
                if (devices.isNotEmpty()) {
                    binding.tvSelectMyDevice.visibility = View.VISIBLE
                    binding.rvMyDevices.visibility = View.VISIBLE
                    deviceAdapter.updateList(devices)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Solo cerramos la actividad si estamos EN EL PASO 1 de una COTIZACIÓN NUEVA
                if (viewModel.isQuotationOnly && viewModel.currentStep.value == 1) {
                    binding.btnNextStep1.text = if (state.isLoading) "Enviando..." else "Enviar Solicitud"
                    
                    if (state.isSuccess) {
                        Toast.makeText(requireContext(), "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun validateFields() {
        val brandModel = binding.etBrandModel.text.toString()
        val serial = binding.etSerial.text.toString()
        val description = binding.etDescription.text.toString()
        
        val hasImage = viewModel.selectedImageUri.value != null || 
                       viewModel.state.value.request.photoUrl.isNotEmpty()

        val isValid = if (isUsingStoredDevice) {
            description.length >= 10 && (hasImage || viewModel.isQuotationOnly)
        } else {
            brandModel.isNotBlank() && 
            serial.length >= 5 && 
            description.length >= 10 && 
            isCategorySelected &&
            (hasImage || viewModel.isQuotationOnly)
        }

        binding.btnNextStep1.isEnabled = isValid
        binding.btnNextStep1.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.load(uri)
        binding.ivPreview.visibility = View.VISIBLE
        binding.layoutEmptyPhoto.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }

    private fun restoreImagePreview() {
        viewModel.selectedImageUri.value?.let { 
            showImagePreview(it)
            validateFields()
        }
    }

    private fun resetCategoryCards() {
        val cards = listOf(binding.catSmartphone, binding.catLaptop, binding.catTablet, binding.catOthers)
        cards.forEach { card ->
            card.setStrokeColor(requireContext().getColor(R.color.outline_variant))
            card.strokeWidth = 2
        }
    }

    private fun selectCategory(category: String, selectedCard: com.google.android.material.card.MaterialCardView) {
        viewModel.updateCategory(category)
        isCategorySelected = true
        resetCategoryCards()
        selectedCard.setStrokeColor(requireContext().getColor(R.color.brand_blue))
        selectedCard.strokeWidth = 6
        validateFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
