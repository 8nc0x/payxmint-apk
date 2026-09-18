package com.payxmint.myapp.ui.checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.TrustGreen
import com.payxmint.myapp.ui.theme.TrustGreenBg
import com.payxmint.myapp.ui.theme.TrustGreenBorder

@Composable
fun TrustFooter(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row of 3 badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustBadge(text = "🔒 256-bit SSL")
            TrustBadge(text = "🛡 PCI-DSS")
            TrustBadge(text = "✓ Verified")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Powered by PayxMint
        Text(
            text = buildAnnotatedString {
                append("🔒 Powered by ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PayxmintBlue)) {
                    append("PayxMint")
                }
            },
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun TrustBadge(text: String) {
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
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
