package com.payxmint.myapp.ui.result

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.ui.theme.ErrorRed
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy
import com.payxmint.myapp.ui.theme.SuccessGreen

@Composable
fun PaymentResultScreen(
    isSuccess: Boolean,
    orderId: String,
    amount: Double,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSuccess) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFECFDF5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(SuccessGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment Received!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = PayxmintNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Transaction verified by PayXMint",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Paid", color = Color(0xFF64748B), fontSize = 14.sp)
                            Text(
                                "₹%.2f".format(amount),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = SuccessGreen
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Order ID", color = Color(0xFF64748B), fontSize = 14.sp)
                            Text(
                                orderId,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = PayxmintNavy
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Gateway", color = Color(0xFF64748B), fontSize = 14.sp)
                            Text("PayXMint UPI", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PayxmintBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PayxmintBlue)
                ) {
                    Text(
                        text = "Back to Store",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            } else {
                // Expired / Failed State
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(ErrorRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment Expired",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = PayxmintNavy
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The payment window elapsed before confirmation.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PayxmintNavy)
                ) {
                    Text(
                        text = "Try Again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
