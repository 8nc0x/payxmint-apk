package com.payxmint.sdk.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.sdk.api.PayxmintApiClient
import com.payxmint.sdk.model.CheckStatusRequest
import com.payxmint.sdk.model.CreateIntentRequest
import com.payxmint.sdk.model.CreateIntentResponse
import com.payxmint.sdk.util.IntentLauncher
import com.payxmint.sdk.util.QrCodeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

// Theme Colors
private val SdkPayxmintBlue = Color(0xFF1E60FF)
private val SdkPayxmintDarkBlue = Color(0xFF0D47A1)
private val SdkPayxmintNavy = Color(0xFF0F172A)
private val SdkPhonePePurple = Color(0xFF5F259F)
private val SdkPaytmCyan = Color(0xFF00BAF2)
private val SdkTimerBg = Color(0xFFFFFBEB)
private val SdkTimerText = Color(0xFFB45309)
private val SdkTimerBorder = Color(0xFFFDE68A)
private val SdkTrustGreen = Color(0xFF059669)
private val SdkTrustGreenBg = Color(0xFFECFDF5)
private val SdkTrustGreenBorder = Color(0xFFA7F3D0)

sealed interface SdkPaymentState {
    object Idle : SdkPaymentState
    object CreatingIntent : SdkPaymentState
    data class Ready(
        val intent: CreateIntentResponse,
        val remainingSeconds: Int = 900,
        val isPolling: Boolean = true
    ) : SdkPaymentState
    data class Success(val orderId: String, val amount: Double) : SdkPaymentState
    data class Expired(val orderId: String) : SdkPaymentState
    data class Error(val message: String) : SdkPaymentState
}

/**
 * Drop-in PayXMint Native Android Checkout Composable
 */
@Composable
fun PayxmintCheckout(
    amount: Double,
    apiKey: String,
    onSuccess: (orderId: String, amount: Double) -> Unit,
    onFailure: (errorMessage: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    orderId: String = remember(amount) { "ord" + System.currentTimeMillis() + (100..999).random() },
    customerMobile: String = "9999999999",
    customerEmail: String = "customer@payxmint.com"
) {
    val context = LocalContext.current
    var paymentState by remember { mutableStateOf<SdkPaymentState>(SdkPaymentState.Idle) }
    var remainingSeconds by remember { mutableIntStateOf(900) }

    // Initiate Intent
    fun initiatePayment() {
        paymentState = SdkPaymentState.CreatingIntent
    }

    LaunchedEffect(Unit) {
        initiatePayment()
    }

    // Intent creation and status polling
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is SdkPaymentState.CreatingIntent -> {
                try {
                    val formattedAmount = String.format(Locale.US, "%.2f", amount)
                    val response = PayxmintApiClient.service.createIntent(
                        authorization = "Bearer $apiKey",
                        request = CreateIntentRequest(
                            amount = formattedAmount,
                            orderId = orderId,
                            customerMobile = customerMobile,
                            customerEmail = customerEmail
                        )
                    )
                    if (response.isSuccessful && response.body() != null) {
                        paymentState = SdkPaymentState.Ready(response.body()!!)
                    } else {
                        val error = response.errorBody()?.string() ?: "Failed to initialize payment"
                        paymentState = SdkPaymentState.Error(error)
                    }
                } catch (e: Exception) {
                    paymentState = SdkPaymentState.Error(e.localizedMessage ?: "Network error")
                }
            }

            is SdkPaymentState.Ready -> {
                // Background polling
                while (isActive && remainingSeconds > 0) {
                    delay(3500)
                    remainingSeconds -= 3
                    try {
                        val statusResp = PayxmintApiClient.service.checkStatus(
                            authorization = "Bearer $apiKey",
                            request = CheckStatusRequest(orderId = orderId)
                        )
                        if (statusResp.isSuccessful && statusResp.body() != null) {
                            when (statusResp.body()!!.status.uppercase()) {
                                "SUCCESS" -> {
                                    paymentState = SdkPaymentState.Success(orderId, amount)
                                    onSuccess(orderId, amount)
                                    return@LaunchedEffect
                                }
                                "EXPIRED" -> {
                                    paymentState = SdkPaymentState.Expired(orderId)
                                    onFailure("Payment expired")
                                    return@LaunchedEffect
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Keep polling on transient network hiccups
                    }
                }

                if (remainingSeconds <= 0) {
                    paymentState = SdkPaymentState.Expired(orderId)
                    onFailure("Payment timed out")
                }
            }

            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            SdkHeader()

            when (val state = paymentState) {
                is SdkPaymentState.CreatingIntent, SdkPaymentState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SdkPayxmintBlue)
                    }
                }

                is SdkPaymentState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Payment Failed", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SdkPayxmintNavy)
                            Text(state.message, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))) {
                                    Text("Cancel", color = SdkPayxmintNavy)
                                }
                                Button(onClick = { initiatePayment() }, colors = ButtonDefaults.buttonColors(containerColor = SdkPayxmintBlue)) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }
                }

                is SdkPaymentState.Ready -> {
                    val intent = state.intent
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Amount & Timer
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Amount to pay", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                text = "₹${if (amount % 1.0 == 0.0) amount.toInt() else amount}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SdkPayxmintNavy
                            )
                            val minutes = remainingSeconds / 60
                            val seconds = remainingSeconds % 60
                            Box(
                                modifier = Modifier
                                    .background(SdkTimerBg, RoundedCornerShape(14.dp))
                                    .border(1.dp, SdkTimerBorder, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 3.dp)
                            ) {
                                Text("⏱ %02d:%02d".format(minutes, seconds), color = SdkTimerText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. Dynamic QR
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val rawQr = intent.qrData ?: intent.upiLink ?: ""
                            SdkQrCard(qrData = rawQr)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(0.5f).height(3.dp),
                                color = SdkPayxmintBlue,
                                trackColor = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Waiting for payment confirmation...", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        // 3. UPI Buttons
                        SdkUpiButtons(
                            onPhonePeClick = {
                                IntentLauncher.launchUpiIntent(
                                    context = context,
                                    primaryUrl = intent.upiLinks?.phonepe,
                                    fallbackUrl = intent.upiLink,
                                    appName = "PhonePe"
                                )
                            },
                            onGooglePayClick = {
                                IntentLauncher.launchGooglePayShare(
                                    context = context,
                                    qrData = intent.qrData ?: intent.upiLink ?: "",
                                    amount = intent.amount,
                                    orderId = intent.orderId
                                )
                            },
                            onPaytmClick = {
                                IntentLauncher.launchUpiIntent(
                                    context = context,
                                    primaryUrl = intent.upiLinks?.paytm,
                                    fallbackUrl = intent.upiLink,
                                    appName = "Paytm"
                                )
                            }
                        )

                        // 4. Trust Footer
                        SdkTrustFooter()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SdkHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(SdkPayxmintBlue, SdkPayxmintDarkBlue)))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF155DFC), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("PayXMint", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("🛡 100% secure · PayxMint", color = Color(0xCCFFFFFF), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SdkQrCard(qrData: String) {
    val qrBitmap = remember(qrData) { QrCodeGenerator.generateQrBitmap(qrData, 512) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Scan QR to pay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SdkPayxmintNavy)
        Text("Open any UPI app on your phone", fontSize = 11.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(136.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(136.dp)) {
                    val stroke = 3.dp.toPx()
                    val bracketLen = 14.dp.toPx()
                    val bracketColor = Color(0xFF0084FF)
                    val inset = 3.dp.toPx()
                    // Brackets
                    drawLine(bracketColor, Offset(inset, inset), Offset(inset + bracketLen, inset), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(inset, inset), Offset(inset, inset + bracketLen), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(size.width - inset - bracketLen, inset), Offset(size.width - inset, inset), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(inset, size.height - inset - bracketLen), Offset(inset, size.height - inset), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(size.width - inset - bracketLen, size.height - inset), Offset(size.width - inset, size.height - inset), stroke, StrokeCap.Round)
                    drawLine(bracketColor, Offset(size.width - inset, size.height - inset - bracketLen), Offset(size.width - inset, size.height - inset), stroke, StrokeCap.Round)
                }
                if (qrBitmap != null) {
                    Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR", modifier = Modifier.size(118.dp))
                }
            }
        }
    }
}

@Composable
private fun SdkUpiButtons(
    onPhonePeClick: () -> Unit,
    onGooglePayClick: () -> Unit,
    onPaytmClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
            Text("OR TAP BELOW", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // PhonePe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(2.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(SdkPhonePePurple)
                .clickable { onPhonePeClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Text("पे", color = SdkPhonePePurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("PhonePe", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Google Pay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(2.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(22.dp))
                .clickable { onGooglePayClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", color = Color(0xFF4285F4), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(5.dp))
                Text("Pay", color = Color(0xFF3C4043), fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Paytm
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .shadow(2.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(SdkPaytmCyan)
                .clickable { onPaytmClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("paytm", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SdkTrustFooter() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SdkBadge("🔒 256-bit SSL")
            SdkBadge("🛡 PCI-DSS")
            SdkBadge("✓ Verified")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append("🔒 Powered by ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SdkPayxmintBlue)) {
                    append("PayxMint")
                }
            },
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun SdkBadge(text: String) {
    Box(
        modifier = Modifier
            .background(SdkTrustGreenBg, RoundedCornerShape(12.dp))
            .border(1.dp, SdkTrustGreenBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = SdkTrustGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
