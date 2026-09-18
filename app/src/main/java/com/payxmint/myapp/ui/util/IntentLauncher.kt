package com.payxmint.myapp.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider

object IntentLauncher {

    /**
     * PayXMint-documented Google Pay native image sharing flow.
     * Generates a 5:4 payment card voucher and launches Android ACTION_SEND chooser.
     */
    fun launchGooglePayShare(
        context: Context,
        qrData: String,
        amount: Double,
        orderId: String,
        onError: (String) -> Unit = {}
    ) {
        try {
            val voucherFile = GPayVoucherGenerator.createPaymentCardImage(
                context = context,
                qrData = qrData,
                amount = amount,
                orderId = orderId
            )

            if (voucherFile == null || !voucherFile.exists()) {
                onError("Failed to generate payment voucher image")
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val qrFileUri = FileProvider.getUriForFile(context, authority, voucherFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, qrFileUri)
                putExtra(Intent.EXTRA_TEXT, "Pay ₹%.2f with Google Pay (Order #%s)".format(amount, orderId))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Pay with Google Pay").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "Unable to open Google Pay share: ${e.localizedMessage ?: "Unknown error"}"
            onError(errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches a specific UPI deep link (e.g. PhonePe, Paytm, or generic UPI).
     * Automatically falls back to generic UPI link or shows a toast if app not found.
     */
    fun launchUpiIntent(
        context: Context,
        primaryUrl: String?,
        fallbackUrl: String? = null,
        appName: String,
        onError: (String) -> Unit = {}
    ) {
        val targetUrl = if (!primaryUrl.isNullOrBlank()) primaryUrl else fallbackUrl

        if (targetUrl.isNullOrBlank()) {
            val msg = "$appName payment link is not available"
            onError(msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If primary failed, try fallback if different
            if (!fallbackUrl.isNullOrBlank() && fallbackUrl != targetUrl) {
                try {
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                    return
                } catch (_: ActivityNotFoundException) {}
            }
            val msg = "$appName is not installed on this device"
            onError(msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = "Could not open $appName: ${e.localizedMessage ?: "Error"}"
            onError(msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
