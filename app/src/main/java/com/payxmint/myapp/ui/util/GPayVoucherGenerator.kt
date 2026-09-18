package com.payxmint.myapp.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object GPayVoucherGenerator {

    /**
     * Generates a 1000 x 800 px (5:4 aspect ratio) payment card voucher image
     * with an 85% central safe zone per PayXMint documentation.
     */
    fun createPaymentCardImage(
        context: Context,
        qrData: String,
        amount: Double,
        orderId: String
    ): File? {
        return try {
            val width = 1000
            val height = 800

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background - Soft clean tech gradient background
            val bgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F4F7FB")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Central Card (within 85% safe zone)
            // 85% of 1000 = 850, 85% of 800 = 680
            val cardLeft = 75f
            val cardTop = 60f
            val cardRight = 925f
            val cardBottom = 740f

            val cardPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                setShadowLayer(16f, 0f, 6f, Color.parseColor("#20000000"))
            }
            val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
            canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

            // Top Header Accent (PayXMint Blue)
            val headerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0066FF")
            }
            val headerRect = RectF(cardLeft, cardTop, cardRight, cardTop + 72f)
            canvas.drawRoundRect(headerRect, 32f, 32f, headerPaint)
            // Fill bottom corners of header
            canvas.drawRect(cardLeft, cardTop + 36f, cardRight, cardTop + 72f, headerPaint)

            // Brand Text
            val brandPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("PayXMint • Secure UPI Checkout", width / 2f, cardTop + 46f, brandPaint)

            // Amount Text
            val amountLabelPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#64748B")
                textSize = 24f
                typeface = Typeface.DEFAULT
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Amount to Pay", width / 2f, cardTop + 120f, amountLabelPaint)

            val amountValuePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0F172A")
                textSize = 52f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val formattedAmount = String.format(Locale.getDefault(), "₹%.2f", amount)
            canvas.drawText(formattedAmount, width / 2f, cardTop + 180f, amountValuePaint)

            // QR Code Bitmap (340x340)
            val qrSize = 340
            val qrBitmap = QrCodeGenerator.generateQrBitmap(
                content = qrData,
                size = qrSize,
                darkColor = Color.parseColor("#0F172A")
            )

            if (qrBitmap != null) {
                val qrLeft = (width - qrSize) / 2f
                val qrTop = cardTop + 205f

                // QR Border box
                val qrBorderPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#E2E8F0")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                val qrRect = RectF(qrLeft - 10f, qrTop - 10f, qrLeft + qrSize + 10f, qrTop + qrSize + 10f)
                canvas.drawRoundRect(qrRect, 16f, 16f, qrBorderPaint)

                canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
            }

            // Instructions / Order Info
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#334155")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Pay with Google Pay", width / 2f, cardBottom - 50f, footerPaint)

            val orderPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 16f
                typeface = Typeface.DEFAULT
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Order #$orderId", width / 2f, cardBottom - 24f, orderPaint)

            // Save to cacheDir / shared_images / payxmint_payment_card.png
            val imagesDir = File(context.cacheDir, "shared_images").apply {
                if (!exists()) mkdirs()
            }
            val voucherFile = File(imagesDir, "payxmint_payment_card.png")
            FileOutputStream(voucherFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            voucherFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
