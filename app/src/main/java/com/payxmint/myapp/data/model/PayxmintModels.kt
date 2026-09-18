package com.payxmint.myapp.data.model

import com.google.gson.annotations.SerializedName

data class CreateIntentRequest(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("customer_mobile")
    val customerMobile: String? = "9876543210",
    @SerializedName("customer_email")
    val customerEmail: String? = "test@payxmint.com",
    @SerializedName("redirect_url")
    val redirectUrl: String? = "https://payxmint.com/thankyou",
    @SerializedName("gateway")
    val gateway: String? = null,
    @SerializedName("metadata")

    val metadata: Map<String, String>? = emptyMap()
)

data class UpiLinks(
    @SerializedName("upi")
    val upi: String? = null,
    @SerializedName("gpay")
    val gpay: String? = null,
    @SerializedName("phonepe")
    val phonepe: String? = null,
    @SerializedName("paytm")
    val paytm: String? = null
)

data class CreateIntentResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("object")
    val objectType: String? = null,
    @SerializedName("amount")
    val amount: Double = 0.0,
    @SerializedName("currency")
    val currency: String? = "INR",
    @SerializedName("status")
    val status: String = "PENDING",
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("checkout_url")
    val checkoutUrl: String? = null,
    @SerializedName("payment_token")
    val paymentToken: String? = null,
    @SerializedName("upi_link")
    val upiLink: String? = null,
    @SerializedName("upi_links")
    val upiLinks: UpiLinks? = null,
    @SerializedName("qr_data")
    val qrData: String? = null,
    @SerializedName("created")
    val created: Long? = null
)

data class CheckStatusRequest(
    @SerializedName("order_id")
    val orderId: String
)

data class PayerInfo(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("upi")
    val upi: String? = null
)

data class SettlementInfo(
    @SerializedName("utr")
    val utr: String? = null,
    @SerializedName("txn_id")
    val txnId: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class CheckStatusResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("object")
    val objectType: String? = null,
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("status")
    val status: String = "PENDING", // PENDING, SUCCESS, EXPIRED
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("payer")
    val payer: PayerInfo? = null,
    @SerializedName("settlement")
    val settlement: SettlementInfo? = null,
    @SerializedName("created")
    val created: Long? = null,
    @SerializedName("expire_at")
    val expireAt: Long? = null
)

data class SimulatePaymentRequest(
    @SerializedName("order_id")
    val orderId: String
)

data class ApiErrorResponse(
    @SerializedName("error")
    val error: ApiErrorDetail? = null
)

data class ApiErrorDetail(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("message")
    val message: String? = null
)
