package com.cibertec.cibertecapp.features.repairs.presentation.activities

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.cibertec.cibertecapp.R
import com.cibertec.cibertecapp.databinding.ActivityPaymentReceiptBinding
import com.cibertec.cibertecapp.features.home.presentation.activities.HomeActivity
import com.cibertec.cibertecapp.features.repairs.domain.model.RepairRequest
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class PaymentReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentReceiptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("REPAIR_REQUEST", RepairRequest::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("REPAIR_REQUEST")
        }

        request?.let { 
            displayReceipt(it)
            setupPdfListener(it)
        }

        setupListeners()
    }

    private fun displayReceipt(request: RepairRequest) {
        // Lógica dinámica para Etiquetas e IDs
        if (request.paymentMethod == "Tienda") {
            binding.tvReceiptOrderLabel.text = "ID DE SOLICITUD"
            binding.tvReceiptOrderId.text = request.orderId
            binding.tvReceiptDateLabel.text = "FECHA SOLICITUD"
            
            binding.tvSuccessTitle.text = "¡Solicitud Confirmada!"
            binding.tvSuccessMessage.text = "El pago será presencial."
            binding.ivSuccessIcon.setImageResource(R.drawable.ic_home)
            binding.tvReceiptTotalLabel.text = "Total a Pagar"
            binding.tvStatusDescription.text = "Hemos recibido tu solicitud. El técnico está a la espera de recibir tu equipo para iniciar el diagnóstico oficial."
            binding.btnSendEmail.visibility = View.GONE
            binding.btnDownloadPdf.visibility = View.GONE
        } else {
            binding.tvReceiptOrderLabel.text = "ID DE TRANSACCIÓN"
            binding.tvReceiptOrderId.text = if (request.paymentId.isNotEmpty()) request.paymentId else request.orderId
            binding.tvReceiptDateLabel.text = "FECHA PAGO"
            
            binding.tvSuccessTitle.text = "¡Pago Exitoso!"
            binding.ivSuccessIcon.setImageResource(R.drawable.ic_check)
            binding.tvStatusDescription.text = "¡Pago verificado! Tu equipo ha sido ingresado al taller y nuestros técnicos han comenzado el proceso de reparación."
            binding.btnSendEmail.visibility = View.VISIBLE
            binding.btnDownloadPdf.visibility = View.VISIBLE
            binding.btnSendEmail.setOnClickListener { sendReceiptEmail(request) }
        }

        // Fecha real capturada del sistema
        binding.tvReceiptDate.text = request.formattedDate
        
        binding.tvReceiptDeviceName.text = request.brandAndModel
        binding.tvReceiptServiceType.text = "Reparación ${request.serviceType}"
        
        binding.tvReceiptBaseCost.text = String.format(Locale.getDefault(), "S/.%.2f", request.baseCost + request.tax)
        binding.tvReceiptTax.text = String.format(Locale.getDefault(), "S/.%.2f", request.tax)
        binding.tvReceiptTotal.text = String.format(Locale.getDefault(), "S/.%.2f", request.total)
    }

    private fun setupPdfListener(request: RepairRequest) {
        binding.btnDownloadPdf.setOnClickListener { generateAndOpenPdf(request) }
    }

    private fun generateAndOpenPdf(request: RepairRequest) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- ESTILO PROFESIONAL ---
        val brandColor = Color.parseColor("#003D9B")
        val lightGray = Color.parseColor("#F5F5F5")
        val dividerColor = Color.parseColor("#EEEEEE")

        // 1. Header con fondo
        paint.color = brandColor
        canvas.drawRect(0f, 0f, 595f, 120f, paint)

        titlePaint.color = Color.WHITE
        titlePaint.textSize = 30f
        titlePaint.isFakeBoldText = true
        canvas.drawText("REPARATECH", 50f, 65f, titlePaint)
        
        paint.color = Color.WHITE
        paint.textSize = 12f
        canvas.drawText("COMPROBANTE ELECTRÓNICO", 50f, 90f, paint)

        // 2. Estado Pagado
        paint.color = Color.parseColor("#4CAF50")
        canvas.drawRect(450f, 40f, 545f, 80f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("PAGADO", 497.5f, 65f, paint)

        // 3. Info de la Orden (Bloque Gris)
        paint.textAlign = Paint.Align.LEFT
        paint.color = lightGray
        canvas.drawRect(50f, 150f, 545f, 230f, paint)
        
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("ID DE ORDEN:", 70f, 180f, paint)
        canvas.drawText("FECHA EMISIÓN:", 70f, 205f, paint)
        canvas.drawText("TRANSACCIÓN ID:", 300f, 180f, paint)
        
        paint.isFakeBoldText = false
        canvas.drawText(request.orderId, 150f, 180f, paint)
        canvas.drawText(request.formattedDate.ifEmpty { "Hoy" }, 165f, 205f, paint)
        canvas.drawText(request.paymentId.ifEmpty { "N/A" }, 400f, 180f, paint)

        // 4. Detalle del Equipo
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("DETALLES DEL SERVICIO", 50f, 280f, paint)
        canvas.drawLine(50f, 290f, 545f, 290f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 12f
        canvas.drawText("Equipo:", 50f, 320f, paint)
        paint.isFakeBoldText = true
        canvas.drawText(request.brandAndModel, 120f, 320f, paint)
        
        paint.isFakeBoldText = false
        canvas.drawText("Servicio:", 50f, 350f, paint)
        paint.isFakeBoldText = true
        canvas.drawText(request.serviceType, 120f, 350f, paint)

        // 5. Resumen de Costos
        canvas.drawLine(50f, 400f, 545f, 400f, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = false
        canvas.drawText("Subtotal:", 400f, 440f, paint)
        canvas.drawText(String.format("S/. %.2f", request.baseCost + request.tax), 545f, 440f, paint)
        
        canvas.drawText("Gastos de envío/prioridad:", 400f, 470f, paint)
        canvas.drawText(String.format("S/. %.2f", request.additionalCost), 545f, 470f, paint)

        // 6. TOTAL FINAL
        paint.textSize = 22f
        paint.isFakeBoldText = true
        paint.color = brandColor
        canvas.drawText("TOTAL:", 400f, 520f, paint)
        canvas.drawText(String.format("S/. %.2f", request.total), 545f, 520f, paint)

        // 7. Footer
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Gracias por confiar en ReparaTech Trujillo.", 595f/2, 750f, paint)
        canvas.drawText(" Salaverry 13611, Trujillo - Perú | soporte@reparatech.com", 595f/2, 770f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Factura_${request.orderId}.pdf"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Comprobante generado", Toast.LENGTH_SHORT).show()
            openPdf(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun openPdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Abrir con..."))
    }

    private fun sendReceiptEmail(request: RepairRequest) {
        val subject = "Recibo ReparaTech - ${request.orderId}"
        val dateLabel = if (request.paymentMethod == "Tienda") "Fecha Solicitud" else "Fecha Pago"
        val idLabel = if (request.paymentMethod == "Tienda") "ID Solicitud" else "ID Transacción"
        val idValue = if (request.paymentId.isNotEmpty()) request.paymentId else request.orderId

        val body = """
            Detalles de tu orden en ReparaTech:
            
            $idLabel: $idValue
            $dateLabel: ${request.formattedDate}
            
            Equipo: ${request.brandAndModel}
            Servicio: ${request.serviceType}
            Total: S/.${request.total}
            
            Gracias por confiar en ReparaTech Trujillo.
        """.trimIndent()
        
        val uriText = "mailto:soporte@reparatech.com" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body)
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(uriText)))
        } catch (e: Exception) { }
    }

    private fun setupListeners() {
        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }
}
