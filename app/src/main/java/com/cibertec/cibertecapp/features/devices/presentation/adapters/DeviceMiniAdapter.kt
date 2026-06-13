package com.cibertec.cibertecapp.features.devices.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ItemDeviceMiniBinding
import com.cibertec.cibertecapp.features.devices.domain.model.Device

class DeviceMiniAdapter(
    private val onClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceMiniAdapter.ViewHolder>() {

    private var devices = listOf<Device>()

    fun updateList(newList: List<Device>) {
        devices = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemDeviceMiniBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.tvDeviceName.text = "${device.brand} ${device.model}"
            binding.ivDeviceThumb.load(device.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_laptop)
            }
            binding.root.setOnClickListener { onClick(device) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceMiniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size
}
