package com.cibertec.cibertecapp.features.requests.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ItemQuotationCardBinding
import com.cibertec.cibertecapp.features.requests.domain.model.QuotationRequest
import java.util.Locale

class QuotationAdapter(
    private val onClick: (QuotationRequest) -> Unit,
    private val onRejectClick: ((QuotationRequest) -> Unit)? = null
) : RecyclerView.Adapter<QuotationAdapter.ViewHolder>() {

    private var requests = listOf<QuotationRequest>()

    fun updateList(newList: List<QuotationRequest>) {
        requests = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemQuotationCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(req: QuotationRequest) {
            val context = binding.root.context
            binding.tvReqId.text = "#REQ-${req.id.takeLast(5).uppercase()}"
            binding.tvDeviceName.text = req.brandAndModel
            binding.tvDescription.text = req.problemDescription
            binding.tvPrice.text = String.format(Locale.getDefault(), "S/. %.2f", req.estimatedPrice)
            binding.tvStatus.text = req.status.uppercase()

            // Admin Comment logic
            if (req.adminComment.isNotEmpty()) {
                binding.layoutAdminComment.visibility = View.VISIBLE
                binding.tvAdminComment.text = req.adminComment
            } else {
                binding.layoutAdminComment.visibility = View.GONE
            }

            // Status colors and Button logic
            if (req.photoUrl.isEmpty()) {
                binding.tvDeviceName.text = "Consulta: ${req.brandAndModel}"
                binding.tvReqId.text = "#TICKET-${req.id.takeLast(5).uppercase()}"
            }

            when (req.status.uppercase()) {
                "COTIZADO" -> {
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.status_green_bg))
                    binding.tvStatus.setTextColor(context.getColor(R.color.status_green_text))
                    binding.btnAction.text = "Aceptar"
                    binding.btnReject.visibility = View.VISIBLE
                    binding.btnReject.text = "Rechazar"
                }
                "RECHAZADO" -> {
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.status_orange_bg))
                    binding.tvStatus.setTextColor(context.getColor(R.color.status_orange_text))
                    binding.btnAction.text = "Ver Motivo"
                    binding.btnReject.visibility = View.VISIBLE
                    binding.btnReject.text = "Eliminar"
                }
                else -> {
                    // PENDIENTE
                    binding.cardStatus.setCardBackgroundColor(context.getColor(R.color.brand_blue_light))
                    binding.tvStatus.setTextColor(context.getColor(R.color.brand_blue))
                    binding.btnAction.text = "Ver Detalles"
                    binding.btnReject.visibility = View.VISIBLE
                    binding.btnReject.text = "Cancelar"
                }
            }

            binding.root.setOnClickListener { onClick(req) }
            binding.btnAction.setOnClickListener { onClick(req) }
            binding.btnReject.setOnClickListener { onRejectClick?.invoke(req) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuotationCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size
}
