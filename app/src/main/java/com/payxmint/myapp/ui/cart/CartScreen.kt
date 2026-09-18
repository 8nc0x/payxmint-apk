package com.payxmint.myapp.ui.cart

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.data.model.CartItem
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintDarkBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    onProceedToCheckout: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsMap by cartViewModel.items.collectAsState()
    val itemsList = itemsMap.values.toList()
    val totalAmount = cartViewModel.totalAmount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shopping Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(text = "←", color = Color.White, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PayxmintDarkBlue)
            )
        },
        bottomBar = {
            if (itemsList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0))
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = { onProceedToCheckout(totalAmount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PayxmintBlue)
                    ) {
                        Text(
                            text = "Pay ₹%.2f with PayXMint".format(totalAmount),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        if (itemsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛒", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your cart is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PayxmintNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = PayxmintBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Products")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(itemsList) { item ->
                    CartItemRow(
                        item = item,
                        onAdd = { cartViewModel.addProduct(item.product) },
                        onRemove = { cartViewModel.removeProduct(item.product) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OrderSummaryCard(totalAmount = totalAmount)
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.product.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PayxmintNavy
                )
                Text(
                    text = "₹%.2f each".format(item.product.price),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "₹%.2f".format(item.lineTotal),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PayxmintBlue,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${item.quantity}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PayxmintBlue)
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(totalAmount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PayxmintNavy
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = Color(0xFF64748B), fontSize = 14.sp)
                Text("₹%.2f".format(totalAmount), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Convenience Fee", color = Color(0xFF64748B), fontSize = 14.sp)
                Text("FREE", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE2E8F0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PayxmintNavy)
                Text("₹%.2f".format(totalAmount), fontWeight = FontWeight.Black, fontSize = 20.sp, color = PayxmintBlue)
            }
        }
    }
}
