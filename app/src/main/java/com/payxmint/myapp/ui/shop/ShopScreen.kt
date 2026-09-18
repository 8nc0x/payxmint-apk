package com.payxmint.myapp.ui.shop

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payxmint.myapp.data.model.Catalog
import com.payxmint.myapp.data.model.Product
import com.payxmint.myapp.ui.cart.CartViewModel
import com.payxmint.myapp.ui.theme.PayxmintBlue
import com.payxmint.myapp.ui.theme.PayxmintDarkBlue
import com.payxmint.myapp.ui.theme.PayxmintNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    cartViewModel: CartViewModel,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by cartViewModel.items.collectAsState()
    val totalCount = cartViewModel.totalCount
    val totalAmount = cartViewModel.totalAmount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PayXMint Store",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Native Android Checkout Test",
                            fontSize = 12.sp,
                            color = Color(0xCCFFFFFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PayxmintDarkBlue
                )
            )
        },
        bottomBar = {
            if (totalCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalCount ${if (totalCount == 1) "item" else "items"}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "₹%.2f".format(totalAmount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayxmintNavy
                            )
                        }

                        Button(
                            onClick = onNavigateToCart,
                            colors = ButtonDefaults.buttonColors(containerColor = PayxmintBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "View Cart →",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Test Products Catalog",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayxmintNavy,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(Catalog.testProducts) { product ->
                val quantity = cartItems[product.id]?.quantity ?: 0
                ProductCard(
                    product = product,
                    quantity = quantity,
                    onAdd = { cartViewModel.addProduct(product) },
                    onRemove = { cartViewModel.removeProduct(product) }
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fruit Emoji Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.emoji,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayxmintNavy
                )
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹%.2f".format(product.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayxmintBlue
                )
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    enabled = quantity > 0,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = PayxmintNavy)
                ) {
                    Text(
                        text = "−",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$quantity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PayxmintNavy,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = PayxmintBlue)
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
