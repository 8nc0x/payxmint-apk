package com.payxmint.sdk.model

import com.google.gson.annotations.SerializedName

data class CreateIntentRequest(
    @SerializedName("amount") val amount: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("customer_mobile") val customerMobile: String = "9999999999",
    @SerializedName("customer_email") val customerEmail: String = "customer@payxmint.com",
    @SerializedName("redirect_url") val redirectUrl: String = "https://payxmint.com/thankyou",
    @SerializedName("customer_ip") val customerIp: String = "127.0.0.1",
    @SerializedName("customer_device_id") val customerDeviceId: String = "android_device",
    @SerializedName("gateway") val gateway: String? = null,
    @SerializedName("metadata") val metadata: Map<String, String> = emptyMap()
)

data class CreateIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("checkout_url") val checkoutUrl: String?,
    @SerializedName("payment_token") val paymentToken: String?,
    @SerializedName("upi_link") val upiLink: String?,
    @SerializedName("upi_links") val upiLinks: UpiLinks?,
    @SerializedName("qr_data") val qrData: String?,
    @SerializedName("created") val created: Long
)

data class UpiLinks(
    @SerializedName("upi") val upi: String?,
    @SerializedName("gpay") val gpay: String?,
    @SerializedName("phonepe") val phonepe: String?,
    @SerializedName("paytm") val paytm: String?
)

data class CheckStatusRequest(
    @SerializedName("order_id") val orderId: String
)

data class CheckStatusResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("status") val status: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String?,
    @SerializedName("payer") val payer: PayerInfo?,
    @SerializedName("settlement") val settlement: SettlementInfo?,
    @SerializedName("created") val created: Long?,
    @SerializedName("expire_at") val expireAt: Long?
)

data class PayerInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("upi") val upi: String?
)

data class SettlementInfo(
    @SerializedName("utr") val utr: String?,
    @SerializedName("txn_id") val txnId: String?,
    @SerializedName("timestamp") val timestamp: String?
)
