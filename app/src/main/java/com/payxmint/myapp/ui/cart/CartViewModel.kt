package com.payxmint.myapp.ui.cart

import androidx.lifecycle.ViewModel
import com.payxmint.myapp.data.model.CartItem
import com.payxmint.myapp.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {

    private val _items = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val items: StateFlow<Map<String, CartItem>> = _items.asStateFlow()

    val totalAmount: Double
        get() = _items.value.values.sumOf { it.lineTotal }

    val totalCount: Int
        get() = _items.value.values.sumOf { it.quantity }

    fun addProduct(product: Product) {
        val current = _items.value.toMutableMap()
        val existing = current[product.id]
        if (existing != null) {
            current[product.id] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current[product.id] = CartItem(product = product, quantity = 1)
        }
        _items.value = current
    }

    fun removeProduct(product: Product) {
        val current = _items.value.toMutableMap()
        val existing = current[product.id] ?: return
        if (existing.quantity > 1) {
            current[product.id] = existing.copy(quantity = existing.quantity - 1)
        } else {
            current.remove(product.id)
        }
        _items.value = current
    }

    fun getQuantity(productId: String): Int {
        return _items.value[productId]?.quantity ?: 0
    }

    fun clearCart() {
        _items.value = emptyMap()
    }
}
