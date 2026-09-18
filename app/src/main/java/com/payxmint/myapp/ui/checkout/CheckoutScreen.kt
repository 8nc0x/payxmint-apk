package com.payxmint.myapp.ui.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.data.model.CreateIntentResponse
import com.payxmint.myapp.domain.PaymentUiState
import com.payxmint.myapp.ui.checkout.components.CheckoutHeader
import com.payxmint.myapp.ui.checkout.components.QrCard
import com.payxmint.myapp.ui.checkout.components.TrustFooter
import com.payxmint.myapp.ui.checkout.components.UpiAppButtons
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy
import com.payxmint.myapp.ui.theme.TimerBg
import com.payxmint.myapp.ui.theme.TimerBorder
import com.payxmint.myapp.ui.theme.TimerText
import com.payxmint.myapp.ui.util.IntentLauncher
import java.util.Locale

@Composable
fun CheckoutScreen(
    amount: Double,
    viewModel: CheckoutViewModel,
    onPaymentSuccess: (String, Double) -> Unit,
    onPaymentExpired: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paymentState by viewModel.paymentState.collectAsState()

    // Initiate payment on first composition if Idle
    LaunchedEffect(Unit) {
        if (paymentState is PaymentUiState.Idle) {
            viewModel.initiatePayment(amount)
        }
    }

    // React to terminal payment states
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentUiState.Success -> {
                onPaymentSuccess(state.orderId, state.amount)
            }
            is PaymentUiState.Expired -> {
                onPaymentExpired(state.orderId)
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
            CheckoutHeader()

            when (val state = paymentState) {
                is PaymentUiState.CreatingIntent -> {
                    LoadingState(amount = amount)
                }

                is PaymentUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.initiatePayment(amount) },
                        onBack = onBack
                    )
                }

                is PaymentUiState.Ready -> {
                    ReadyCheckoutContent(
                        intent = state.intent,
                        remainingSeconds = state.remainingSeconds,
                        isPolling = state.isPolling,
                        onPhonePeClick = {
                            IntentLauncher.launchUpiIntent(
                                context = context,
                                primaryUrl = state.intent.upiLinks?.phonepe,
                                fallbackUrl = state.intent.upiLink,
                                appName = "PhonePe"
                            )
                        },
                        onGooglePayClick = {
                            IntentLauncher.launchGooglePayShare(
                                context = context,
                                qrData = state.intent.qrData ?: state.intent.upiLink ?: "",
                                amount = state.intent.amount,
                                orderId = state.intent.orderId
                            )
                        },
                        onPaytmClick = {
                            IntentLauncher.launchUpiIntent(
                                context = context,
                                primaryUrl = state.intent.upiLinks?.paytm,
                                fallbackUrl = state.intent.upiLink,
                                appName = "Paytm"
                            )
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ReadyCheckoutContent(
    intent: CreateIntentResponse,
    remainingSeconds: Int,
    isPolling: Boolean,
    onPhonePeClick: () -> Unit,
    onGooglePayClick: () -> Unit,
    onPaytmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Amount to pay & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Amount to pay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )

            val formattedAmount = if (intent.amount % 1.0 == 0.0) {
                String.format(Locale.getDefault(), "₹%.0f", intent.amount)
            } else {
                String.format(Locale.getDefault(), "₹%.2f", intent.amount)
            }
            Text(
                text = formattedAmount,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PayxmintNavy,
                letterSpacing = (-0.5).sp
            )

            // Countdown Timer Pill
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            Box(
                modifier = Modifier
                    .background(TimerBg, RoundedCornerShape(14.dp))
                    .border(1.dp, TimerBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeString,
                        color = TimerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. QR Section & Polling Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rawQrData = intent.qrData ?: intent.upiLink ?: ""
            QrCard(qrData = rawQrData)

            Spacer(modifier = Modifier.height(6.dp))

            if (isPolling) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(3.dp),
                    color = PayxmintBlue,
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Waiting for payment confirmation...",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }

        // 3. UPI App Buttons (PhonePe, GPay, Paytm)
        UpiAppButtons(
            onPhonePeClick = onPhonePeClick,
            onGooglePayClick = onGooglePayClick,
            onPaytmClick = onPaytmClick
        )

        // 4. Trust Badges & Powered by PayxMint
        TrustFooter()
    }
}

@Composable
private fun LoadingState(amount: Double) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                color = PayxmintBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Connecting to PayXMint...",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = PayxmintNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Creating secure intent for ₹%.2f".format(amount),
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 44.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Payment Initialization Failed",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PayxmintNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
                ) {
                    Text("Back to Cart", color = PayxmintNavy)
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = PayxmintBlue)
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
    }
}
