package com.payxmint.myapp.ui.checkout.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy
import com.payxmint.myapp.ui.util.QrCodeGenerator

@Composable
fun QrCard(
    qrData: String,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(qrData) {
        QrCodeGenerator.generateQrBitmap(
            content = qrData,
            size = 512
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan QR to pay",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = PayxmintNavy
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Open any UPI app on your phone",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // White Container Card with soft blue tint & border
        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x221E60FF))
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Container with QR and 4 corner bracket overlays
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Bracket overlay
                Canvas(modifier = Modifier.size(200.dp)) {
                    val stroke = 3.5.dp.toPx()
                    val bracketLen = 22.dp.toPx()
                    val bracketColor = Color(0xFF0084FF)
                    val inset = 4.dp.toPx()

                    // Top-Left ┌
                    drawLine(
                        color = bracketColor,
                        start = Offset(inset, inset),
                        end = Offset(inset + bracketLen, inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(inset, inset),
                        end = Offset(inset, inset + bracketLen),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )

                    // Top-Right ┐
                    drawLine(
                        color = bracketColor,
                        start = Offset(size.width - inset - bracketLen, inset),
                        end = Offset(size.width - inset, inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(size.width - inset, inset),
                        end = Offset(size.width - inset, inset + bracketLen),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )

                    // Bottom-Left └
                    drawLine(
                        color = bracketColor,
                        start = Offset(inset, size.height - inset),
                        end = Offset(inset + bracketLen, size.height - inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(inset, size.height - inset - bracketLen),
                        end = Offset(inset, size.height - inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )

                    // Bottom-Right ┘
                    drawLine(
                        color = bracketColor,
                        start = Offset(size.width - inset - bracketLen, size.height - inset),
                        end = Offset(size.width - inset, size.height - inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = bracketColor,
                        start = Offset(size.width - inset, size.height - inset - bracketLen),
                        end = Offset(size.width - inset, size.height - inset),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }

                // Center QR Image
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Payment QR Code",
                        modifier = Modifier
                            .size(172.dp)
                            .padding(4.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = PayxmintBlue
                    )
                }
            }
        }
    }
}
