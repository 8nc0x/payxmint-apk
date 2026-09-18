package com.payxmint.sdk.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object IntentLauncher {

    fun launchUpiIntent(
        context: Context,
        primaryUrl: String?,
        fallbackUrl: String?,
        appName: String
    ) {
        val targetUrl = primaryUrl?.takeIf { it.isNotBlank() } ?: fallbackUrl
        if (targetUrl.isNullOrBlank()) {
            Toast.makeText(context, "No payment link available for $appName", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            if (fallbackUrl != null && fallbackUrl != targetUrl) {
                try {
                    val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(genericIntent)
                    return
                } catch (_: Exception) {}
            }
            Toast.makeText(
                context,
                "$appName is not installed on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun launchGooglePayShare(
        context: Context,
        qrData: String,
        amount: Double,
        orderId: String
    ) {
        try {
            val voucherBitmap = createPaymentVoucherCard(context, qrData, amount, orderId)
            val imageUri = saveBitmapToCache(context, voucherBitmap, "payxmint_payment_card.png")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, "PayXMint Payment - Order $orderId")
                putExtra(
                    Intent.EXTRA_TEXT,
                    String.format(Locale.getDefault(), "Pay ₹%.2f securely via Google Pay / UPI", amount)
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Pay with Google Pay / UPI")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open Google Pay share flow: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createPaymentVoucherCard(
        context: Context,
        qrData: String,
        amount: Double,
        orderId: String
    ): Bitmap {
        val width = 1000
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#0F172A"))

        // Card Surface
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val cardRect = RectF(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat())
        canvas.drawRoundRect(cardRect, 36f, 36f, cardPaint)

        // Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E60FF")
        }
        val headerRect = RectF(40f, 40f, (width - 40).toFloat(), 150f)
        canvas.drawRoundRect(headerRect, 36f, 36f, headerPaint)
        canvas.drawRect(40f, 110f, (width - 40).toFloat(), 150f, headerPaint)

        // Header Text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("⚡ PayXMint Checkout", width / 2f, 108f, titlePaint)

        // Amount Display
        val amountLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Amount to Pay", width / 2f, 205f, amountLabelPaint)

        val amountValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 58f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val formattedAmount = if (amount % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "₹%.0f", amount)
        } else {
            String.format(Locale.getDefault(), "₹%.2f", amount)
        }
        canvas.drawText(formattedAmount, width / 2f, 275f, amountValuePaint)

        // QR Code in Center
        val qrBitmap = QrCodeGenerator.generateQrBitmap(qrData, 340)
        if (qrBitmap != null) {
            val qrLeft = (width - 340) / 2f
            val qrTop = 310f
            canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
        }

        // Subtitle / Instruction
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E60FF")
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Scan with Google Pay or any UPI App", width / 2f, 690f, subTextPaint)

        // Footer Order Reference
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Order ID: $orderId  •  100% Secure UPI", width / 2f, 728f, footerPaint)

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): Uri {
        val cachePath = File(context.cacheDir, "payxmint_shared")
        cachePath.mkdirs()

        val file = File(cachePath, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val authority = "${context.packageName}.payxmint.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
