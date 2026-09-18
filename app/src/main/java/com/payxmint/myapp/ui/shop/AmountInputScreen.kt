package com.payxmint.myapp.ui.shop

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintDarkBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy
import com.payxmint.myapp.ui.theme.TrustGreen
import com.payxmint.myapp.ui.theme.TrustGreenBg
import com.payxmint.myapp.ui.theme.TrustGreenBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputScreen(
    onBuyClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var amountInput by remember { mutableStateOf("1") }
    val focusManager = LocalFocusManager.current

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isValidAmount = parsedAmount >= 1.0

    val quickPresets = listOf("1", "2", "5", "10", "50", "100")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF155DFC), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PayXMint",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Automated UPI Payments",
                                fontSize = 11.sp,
                                color = Color(0xCCFFFFFF)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PayxmintDarkBlue
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Payment Amount",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayxmintNavy
                )

                Text(
                    text = "Enter any test amount to start native checkout",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENTER AMOUNT IN RUPEES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Styled Text Field with ₹ prefix
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                // Filter to valid number format (digits and optional single decimal point)
                                if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                    amountInput = input
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayxmintNavy
                            ),
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PayxmintBlue,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            trailingIcon = {
                                if (amountInput.isNotEmpty()) {
                                    IconButton(onClick = { amountInput = "" }) {
                                        Text("✕", color = Color(0xFF94A3B8), fontSize = 16.sp)
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "0",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCBD5E1)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isValidAmount) {
                                        onBuyClick(parsedAmount)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PayxmintBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Presets Row
                        Text(
                            text = "Quick Presets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickPresets.forEach { preset ->
                                val isSelected = amountInput == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) PayxmintBlue else Color(0xFFF1F5F9)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) PayxmintBlue else Color(0xFFE2E8F0),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            amountInput = preset
                                            focusManager.clearFocus()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₹$preset",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else PayxmintNavy
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buy Button placed directly below the amount box
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (isValidAmount) {
                            onBuyClick(parsedAmount)
                        }
                    },
                    enabled = isValidAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = if (isValidAmount) 4.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = PayxmintBlue
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PayxmintBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isValidAmount) "Buy Now  •  ₹${if (parsedAmount % 1.0 == 0.0) parsedAmount.toInt() else parsedAmount}" else "Enter valid amount",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValidAmount) Color.White else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValidAmount) Color.White else Color(0xFF64748B)
                        )
                    }
                }

                if (!isValidAmount && amountInput.isNotEmpty()) {
                    Text(
                        text = "⚠️ Minimum amount is ₹1.00",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Bottom Highlights / Security Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureBadge("⚡ Instant UPI")
                    FeatureBadge("🛡 PayXMint API")
                    FeatureBadge("🔒 Native UI")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🔒 Live automated UPI payment engine",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Box(
        modifier = Modifier
            .background(TrustGreenBg, RoundedCornerShape(12.dp))
            .border(1.dp, TrustGreenBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TrustGreen,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
