package com.cibertec.cibertecapp.features.home.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ItemRepairCardBinding
import com.cibertec.cibertecapp.features.home.domain.model.Repair

class RepairAdapter(
    private val onClick: (Repair) -> Unit
) : RecyclerView.Adapter<RepairAdapter.RepairViewHolder>() {

    private var repairs = listOf<Repair>()

    fun updateList(newList: List<Repair>) {
        repairs = newList
        notifyDataSetChanged()
    }

    inner class RepairViewHolder(
        private val binding: ItemRepairCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(repair: Repair) {
            val context = binding.root.context

            binding.tvRepairName.text =
                repair.deviceName

            binding.tvRepairId.text =
                "${repair.orderId} • ${repair.service}"

            binding.tvProgressPercent.text = "${repair.progress}%"

            binding.tvRepairStatus.text =
                repair.status.uppercase()
            
            when(repair.status.uppercase()) {
                "COMPLETADO" -> {
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.status_green_bg))
                    binding.tvRepairStatus.setTextColor(context.getColor(R.color.status_green_text))
                }
                "PROGRESO" -> {
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.status_blue_bg))
                    binding.tvRepairStatus.setTextColor(context.getColor(R.color.status_blue_text))
                }
                "REVISION" -> {
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.status_orange_bg))
                    binding.tvRepairStatus.setTextColor(context.getColor(R.color.status_orange_text))
                }
                else -> { // PENDIENTE o cualquier otro
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.brand_blue_light))
                    binding.tvRepairStatus.setTextColor(context.getColor(R.color.brand_blue))
                }
            }

            binding.tvDeliveryDate.text =
                "Fecha: ${repair.date}"

            binding.repairProgress.progress =
                repair.progress

            binding.ivRepairIcon.clearColorFilter()

            if (repair.photoUrl.isNotEmpty()) {
                binding.ivRepairIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.ivRepairIcon.setPadding(0, 0, 0, 0)
                binding.ivRepairIcon.load(repair.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_laptop)
                    error(R.drawable.ic_laptop)
                }
            } else {
                binding.ivRepairIcon.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                val padding = (14 * context.resources.displayMetrics.density).toInt()
                binding.ivRepairIcon.setPadding(padding, padding, padding, padding)
                binding.ivRepairIcon.setImageResource(R.drawable.ic_laptop)
                binding.ivRepairIcon.setColorFilter(context.getColor(R.color.brand_blue))
            }

            binding.btnDetails.setOnClickListener {
                onClick(repair)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepairViewHolder {

        val binding = ItemRepairCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RepairViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RepairViewHolder,
        position: Int
    ) {
        holder.bind(repairs[position])
    }

    override fun getItemCount(): Int {
        return repairs.size
    }
}