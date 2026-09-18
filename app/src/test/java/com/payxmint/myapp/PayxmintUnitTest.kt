package com.payxmint.myapp

import com.google.gson.Gson
import com.payxmint.myapp.data.model.Catalog
import com.payxmint.myapp.data.model.CheckStatusRequest
import com.payxmint.myapp.data.model.CheckStatusResponse
import com.payxmint.myapp.data.model.CreateIntentRequest
import com.payxmint.myapp.data.model.CreateIntentResponse
import com.payxmint.myapp.data.model.UpiLinks
import com.payxmint.myapp.ui.cart.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PayxmintUnitTest {

    @Test
    fun testCatalogPricesAndDefinitions() {
        val products = Catalog.testProducts
        assertEquals(2, products.size)

        val apple = products.find { it.name == "Apple" }
        assertNotNull(apple)
        assertEquals(1.0, apple!!.price, 0.001)

        val orange = products.find { it.name == "Orange" }
        assertNotNull(orange)
        assertEquals(2.0, orange!!.price, 0.001)
    }

    @Test
    fun testCartCalculations() {
        val cartViewModel = CartViewModel()
        val products = Catalog.testProducts
        val apple = products[0]
        val orange = products[1]

        // Add 2 Apples (₹2) and 1 Orange (₹2)
        cartViewModel.addProduct(apple)
        cartViewModel.addProduct(apple)
        cartViewModel.addProduct(orange)

        assertEquals(3, cartViewModel.totalCount)
        assertEquals(4.0, cartViewModel.totalAmount, 0.001)
        assertEquals(2, cartViewModel.getQuantity(apple.id))
        assertEquals(1, cartViewModel.getQuantity(orange.id))

        // Remove 1 Apple
        cartViewModel.removeProduct(apple)
        assertEquals(2, cartViewModel.totalCount)
        assertEquals(3.0, cartViewModel.totalAmount, 0.001)

        // Clear cart
        cartViewModel.clearCart()
        assertEquals(0, cartViewModel.totalCount)
        assertEquals(0.0, cartViewModel.totalAmount, 0.001)
    }

    @Test
    fun testCreateIntentRequestSerialization() {
        val gson = Gson()
        val request = CreateIntentRequest(
            amount = "3.00",
            orderId = "tx_12345678",
            customerMobile = "9876543210",
            customerEmail = "test@payxmint.com",
            redirectUrl = "https://payxmint.com/thankyou",
            gateway = "GPAY"
        )
        val json = gson.toJson(request)
        assertTrue(json.contains("\"amount\":\"3.00\""))
        assertTrue(json.contains("\"order_id\":\"tx_12345678\""))
        assertTrue(json.contains("\"gateway\":\"GPAY\""))
    }

    @Test
    fun testCreateIntentResponseDeserialization() {
        val sampleResponseJson = """
        {
          "id": "ptx_8f2a9c3d1e",
          "object": "payment_intent",
          "amount": 1850.00,
          "currency": "INR",
          "status": "PENDING",
          "order_id": "tx_873491023",
          "checkout_url": "https://payxmint.com/pay/ptx_8f2a9c3d1e",
          "payment_token": "ptx_8f2a9c3d1e",
          "upi_link": "upi://pay?pa=merchant@upi&pn=Store&am=1850.00&cu=INR&tn=tx_873491023&tr=tx_873491023",
          "upi_links": {
            "upi": "upi://pay?pa=merchant@upi...",
            "gpay": "intent://...",
            "phonepe": "phonepe://...",
            "paytm": "paytmmp://..."
          },
          "qr_data": "upi://pay?pa=merchant@upi&pn=Store&am=1850.00&cu=INR&tn=tx_873491023&tr=tx_873491023",
          "created": 1779187312
        }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(sampleResponseJson, CreateIntentResponse::class.java)

        assertEquals("ptx_8f2a9c3d1e", response.id)
        assertEquals("PENDING", response.status)
        assertEquals("tx_873491023", response.orderId)
        assertEquals(1850.00, response.amount, 0.001)
        assertNotNull(response.upiLinks)
        assertEquals("phonepe://...", response.upiLinks?.phonepe)
        assertEquals("paytmmp://...", response.upiLinks?.paytm)
        assertNotNull(response.qrData)
    }

    @Test
    fun testCheckStatusResponseDeserialization() {
        val sampleStatusJson = """
        {
          "id": "ptx_8f2a9c3d1e",
          "object": "payment_intent",
          "amount": 1850.00,
          "currency": "INR",
          "status": "SUCCESS",
          "order_id": "tx_873491023",
          "payer": {
            "name": "John Doe",
            "upi": "johndoe@ybl"
          },
          "settlement": {
            "utr": "6120948375",
            "txn_id": "tx_612093847",
            "timestamp": "2026-05-19T17:11:45.000Z"
          },
          "created": 1779187312,
          "expire_at": 1779188212
        }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(sampleStatusJson, CheckStatusResponse::class.java)

        assertEquals("SUCCESS", response.status)
        assertEquals("tx_873491023", response.orderId)
        assertNotNull(response.payer)
        assertEquals("John Doe", response.payer?.name)
        assertEquals("johndoe@ybl", response.payer?.upi)
        assertNotNull(response.settlement)
        assertEquals("6120948375", response.settlement?.utr)
    }
}
