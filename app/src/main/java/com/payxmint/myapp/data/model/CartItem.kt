package com.payxmint.myapp.data.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double
        get() = product.price * quantity
}
