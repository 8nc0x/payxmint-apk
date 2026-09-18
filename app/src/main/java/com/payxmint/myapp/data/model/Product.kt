package com.payxmint.myapp.data.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val emoji: String
)

object Catalog {
    val testProducts: List<Product> = listOf(
        Product(
            id = "prod_apple_1",
            name = "Apple",
            price = 1.0,
            description = "Fresh red apple (Test Product)",
            emoji = "🍎"
        ),
        Product(
            id = "prod_orange_2",
            name = "Orange",
            price = 2.0,
            description = "Juicy citrus orange (Test Product)",
            emoji = "🍊"
        )
    )
}
