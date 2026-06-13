package com.cibertec.cibertecapp.features.devices.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ItemDeviceCardBinding
import com.cibertec.cibertecapp.features.devices.domain.model.Device

class DeviceAdapter(
    private val onClick: (Device, View) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var devices = listOf<Device>()

    fun updateList(newList: List<Device>) {
        devices = newList
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.tvDeviceName.text = "${device.brand} ${device.model}"
            binding.tvDeviceSerial.text = "S/N: ${device.serialNumber}"
            binding.tvDeviceStatus.text = device.category.uppercase()
            
            binding.ivDeviceIcon.load(device.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_laptop)
                error(R.drawable.ic_laptop)
            }
            
            binding.root.setOnClickListener { onClick(device, binding.ivDeviceIcon) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size
}
