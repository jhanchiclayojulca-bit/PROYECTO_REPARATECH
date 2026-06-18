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
        binding.tvReceiptOrderId.text = request.orderId
        binding.tvReceiptDeviceName.text = request.brandAndModel
        binding.tvReceiptServiceType.text = "Reparación ${request.serviceType}"
        binding.tvReceiptBaseCost.text = String.format(Locale.getDefault(), "S/.%.2f", request.baseCost + request.tax)
        binding.tvReceiptTax.text = String.format(Locale.getDefault(), "S/.%.2f", request.tax)
        binding.tvReceiptTotal.text = String.format(Locale.getDefault(), "S/.%.2f", request.total)

        when (request.paymentMethod) {
            "Tienda" -> {
                binding.tvSuccessTitle.text = "¡Solicitud Confirmada!"
                binding.tvSuccessMessage.text = "Tu orden ha sido registrada. Acércate a nuestro taller para el pago."
                binding.ivSuccessIcon.setImageResource(R.drawable.ic_home)
                binding.tvReceiptTotalLabel.text = "Total a Pagar"
                binding.btnSendEmail.visibility = android.view.View.GONE
            }
            else -> {
                binding.btnSendEmail.visibility = android.view.View.VISIBLE
                binding.btnSendEmail.setOnClickListener { sendReceiptEmail(request) }
            }
        }
    }

    private fun setupPdfListener(request: RepairRequest) {
        binding.btnDownloadPdf.setOnClickListener {
            generateAndOpenPdf(request)
        }
    }

    private fun generateAndOpenPdf(request: RepairRequest) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Page info: A4 size (approx 595 x 842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Header
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.parseColor("#003D9B")
        canvas.drawText("REPARATECH - COMPROBANTE", 595f / 2, 80f, titlePaint)

        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("Servicio Técnico de Excelencia", 595f / 2, 105f, paint)
        canvas.drawLine(50f, 130f, 545f, 130f, paint)

        // Details
        var y = 180f
        val leftX = 70f
        val rightX = 525f
        
        paint.isFakeBoldText = true
        canvas.drawText("ID DE ORDEN:", leftX, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(request.orderId.ifEmpty { "N/A" }, leftX + 110, y, paint)
        
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("EQUIPO:", leftX, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(request.brandAndModel, leftX + 100, y, paint)

        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("SERVICIO:", leftX, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(request.serviceType, leftX + 100, y, paint)

        y += 60f
        canvas.drawLine(50f, y, 545f, y, paint)
        
        y += 40f
        paint.isFakeBoldText = true
        canvas.drawText("DESGLOSE DE PAGO", leftX, y, paint)
        
        y += 30f
        paint.isFakeBoldText = false
        canvas.drawText("Costo Base + Tax:", leftX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("S/. %.2f", request.baseCost + request.tax), rightX, y, paint)
        
        y += 25f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Impuestos (18%):", leftX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("S/. %.2f", request.tax), rightX, y, paint)

        y += 40f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", leftX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#003D9B")
        canvas.drawText(String.format("S/. %.2f", request.total), rightX, y, paint)

        pdfDocument.finishPage(page)

        // Save file
        val fileName = "Recibo_${request.orderId}.pdf"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF generado correctamente", Toast.LENGTH_SHORT).show()
            openPdf(file)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
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
        startActivity(Intent.createChooser(intent, "Abrir comprobante con..."))
    }

    private fun sendReceiptEmail(request: RepairRequest) {
        val body = "ID Orden: ${request.orderId}\nEquipo: ${request.brandAndModel}\nTotal: S/.${request.total}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recibo ReparaTech - ${request.orderId}")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Enviar por..."))
        }
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
