# PayXMint — Native Android Integration Guide
### Headless Custom UPI Checkout & Android SDK Integration

Welcome to the official **PayXMint Native Android Integration Guide**. This document provides an exhaustive, step-by-step developer specification for building high-conversion, headless UPI checkout flows inside your native Android applications using Jetpack Compose and the PayXMint automated payment engine.

---

## Table of Contents
1. [Overview & Core Principles](#1-overview--core-principles)
2. [Architecture & Workflow](#2-architecture--workflow)
3. [Dependencies & Gradle Setup](#3-dependencies--gradle-setup)
4. [AndroidManifest & Permissions](#4-androidmanifest--permissions)
5. [PayXMint API Contract](#5-payxmint-api-contract)
   - [Create Payment Intent (`/create-intent`)](#create-payment-intent-create-intent)
   - [Check Status (`/check-status`)](#check-status-check-status)
   - [Critical Parameter Rules](#critical-parameter-rules)
6. [Dynamic QR Code Generation](#6-dynamic-qr-code-generation)
7. [UPI Deep Linking & Google Pay Voucher Architecture](#7-upi-deep-linking--google-pay-voucher-architecture)
   - [PhonePe & Paytm App Intents](#phonepe--paytm-app-intents)
   - [Google Pay Voucher Card Share Flow](#google-pay-voucher-card-share-flow)
8. [Real-time Polling & Expiry Engine](#8-real-time-polling--expiry-engine)
9. [Native Jetpack Compose UI Implementation](#9-native-jetpack-compose-ui-implementation)
   - [Single-Screen Layout Architecture](#single-screen-layout-architecture)
   - [Components Breakdown](#components-breakdown)
10. [Production Security & Webhook Reconciliation](#10-production-security--webhook-reconciliation)
11. [Troubleshooting & Common Pitfalls](#11-troubleshooting--common-pitfalls)
12. [Kotlin SDK Architecture & Publishing Proposal](#12-kotlin-sdk-architecture--publishing-proposal)
13. [Reference App & Source Code Package](#13-reference-app--source-code-package)

---

## 1. Overview & Core Principles

PayXMint provides an automated UPI payment infrastructure. Unlike legacy gateways that force customers through slow WebViews or redirect them to external browser URLs, PayXMint supports **100% Headless Native Custom Checkouts**:

* **Zero WebViews**: WebViews break UPI app-to-app handshakes, cause session drops, and produce high cart abandonment.
* **No `checkout_url` Redirects**: All transaction data (`amount`, `order_id`, `qr_data`, `upi_links`) originates dynamically from the API and is rendered natively in your Jetpack Compose UI.
* **Instant Intent Execution**: Direct invocation of installed UPI apps (**PhonePe**, **Paytm**, **Google Pay**) with sub-second handshakes.
* **Dynamic QR Code**: High-contrast, dynamic QR code generated on-device with ZXing, enabling instant cross-device payment scanning.

---

## 2. Architecture & Workflow

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────────────┐
│   Merchant App  │       │  PayXMint Live API   │       │  UPI App / Banking   │
└────────┬────────┘       └──────────┬───────────┘       └──────────┬───────────┘
         │                           │                              │
         │ 1. POST /create-intent    │                              │
         ├──────────────────────────>│                              │
         │   (amount, order_id)      │                              │
         │                           │                              │
         │ 2. Return Payment Details │                              │
         │<──────────────────────────┤                              │
         │   (qr_data, upi_links)    │                              │
         │                           │                              │
         │ 3. Render Native Checkout │                              │
         │   - Dynamic ZXing QR      │                              │
         │   - PhonePe / GPay / Paytm│                              │
         │                           │                              │
         │ 4. User taps App or Scans │                              │
         ├─────────────────────────────────────────────────────────>│
         │                           │                              │ 5. Customer
         │ 6. Background Polling     │                              │    authorizes UPI
         │    POST /check-status     │                              │    payment
         │    (every 3.5 seconds)    │                              │
         ├──────────────────────────>│                              │
         │                           │ 7. Bank reconciliation       │
         │                           │<─────────────────────────────┤
         │ 8. Status: SUCCESS        │                              │
         │<──────────────────────────┤                              │
         │                           │                              │
         │ 9. Show Success Screen    │                              │
```

---

## 3. Dependencies & Gradle Setup

Add the required libraries to your `app/build.gradle.kts` file:

```kotlin
dependencies {
    // Jetpack Compose & Material 3
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Networking (Retrofit & OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Dynamic QR Code Generation (ZXing Core)
    implementation("com.google.zxing:core:3.5.3")
}
```

---

## 4. AndroidManifest & Permissions

### 4.1 Android 11+ Package Visibility (`<queries>`)
Android 11 (API 30) introduced package visibility restrictions. To detect installed UPI apps and invoke intents cleanly, add this `<queries>` block inside `AndroidManifest.xml` (outside the `<application>` tag):

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Allow discovering and launching UPI payment applications -->
    <queries>
        <package android:name="com.phonepe.app" />
        <package android:name="net.one97.paytm" />
        <package android:name="com.google.android.apps.nbu.paisa.user" />
        <intent>
            <action android:name="android.intent.action.VIEW" />
            <data android:scheme="upi" />
        </intent>
    </queries>

    <application ...>
```

### 4.2 FileProvider for Google Pay Payment Card Share
Google Pay handles direct external peer payments via an Android Image Share Sheet containing a formatted payment voucher. This requires an Android `FileProvider`:

Inside `<application ...>` in `AndroidManifest.xml`:
```xml
        <!-- FileProvider for sharing the Google Pay Payment Voucher Card -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```

Create `app/src/main/res/xml/file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="shared_images/" />
</paths>
```

---

## 5. PayXMint API Contract

### Production Endpoint
```
Base URL: https://payxmint.com/api/v1
Authentication: Bearer <YOUR_SECRET_API_KEY>
```

### 5.1 Data Models (Kotlin)

```kotlin
data class CreateIntentRequest(
    @SerializedName("amount") val amount: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("customer_mobile") val customerMobile: String = "9999999999",
    @SerializedName("customer_email") val customerEmail: String = "customer@payxmint.com",
    @SerializedName("redirect_url") val redirectUrl: String = "https://payxmint.com/thankyou",
    @SerializedName("customer_ip") val customerIp: String = "127.0.0.1",
    @SerializedName("customer_device_id") val customerDeviceId: String = "android_device",
    @SerializedName("gateway") val gateway: String? = null, // ALWAYS omit or pass null!
    @SerializedName("metadata") val metadata: Map<String, String> = emptyMap()
)

data class CreateIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val obj: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("payment_token") val paymentToken: String?,
    @SerializedName("checkout_url") val checkoutUrl: String?,
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
    @SerializedName("status") val status: String, // "PENDING", "SUCCESS", "EXPIRED"
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
```

### 5.2 Critical Parameter Rules

> [!IMPORTANT]
> **1. The `gateway` Parameter MUST Be Omitted**
> In your request payload, **do NOT include `"gateway": "GPAY"`** or any fixed gateway string. Omitting `gateway` (or passing `null`) allows PayXMint's Smart Router to automatically route to healthy, active VPAs. Specifying an unavailable gateway can trigger a `503 ROUTING_ERROR: No available VPAs`.

> [!IMPORTANT]
> **2. Order ID Formatting (`order_id`)**
> `order_id` must be:
> * At least **8 characters** long.
> * **Strictly alphanumeric** (`^[a-zA-Z0-9]+$`).
> * Do NOT include hyphens (`-`), underscores (`_`), or spaces.
> 
> *Recommended generator:*
> ```kotlin
> fun generateOrderId(): String = "ord" + System.currentTimeMillis() + (100..999).random()
> ```

---

## 6. Dynamic QR Code Generation

The API returns payment data in `qr_data` (with fallback to `upi_link`). Never hardcode mock QR images. Generate a crisp, dynamic QR bitmap directly on the device using ZXing:

```kotlin
object QrCodeGenerator {
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) {
                        android.graphics.Color.BLACK
                    } else {
                        android.graphics.Color.WHITE
                    }
                }
            }

            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            null
        }
    }
}
```

---

## 7. UPI Deep Linking & Google Pay Voucher Architecture

### 7.1 PhonePe & Paytm App Intents

When invoking PhonePe or Paytm, use their specific protocol URLs from `intent.upiLinks`:
* **PhonePe**: `intent.upiLinks.phonepe` (format: `phonepe://upi/pay?...`)
* **Paytm**: `intent.upiLinks.paytm` (format: `paytmmp://pay?...`)

```kotlin
fun launchUpiIntent(context: Context, primaryUrl: String?, fallbackUrl: String?, appName: String) {
    val targetUrl = primaryUrl?.takeIf { it.isNotBlank() } ?: fallbackUrl
    if (targetUrl.isNullOrBlank()) {
        Toast.makeText(context, "No UPI link available for $appName", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to generic UPI chooser
        if (fallbackUrl != null && fallbackUrl != targetUrl) {
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(genericIntent)
                return
            } catch (_: Exception) {}
        }
        Toast.makeText(context, "$appName is not installed on this device", Toast.LENGTH_SHORT).show()
    }
}
```

### 7.2 Google Pay Voucher Card Share Flow

#### The Technical Rationale
On standard Android devices, invoking generic UPI deep-links directly into Google Pay often triggers security warnings or silent failures if the merchant VPA has not completed Google's multi-week corporate app whitelisting. 

PayXMint solves this elegantly through the **Google Pay Voucher Card Share Sheet Flow**:
1. When the customer clicks **Google Pay**, the app renders a **1000 × 800 px (5:4 aspect ratio) high-fidelity payment voucher card**.
2. The voucher card includes:
   * PayXMint security header.
   * Prominent transaction amount (`₹{amount}`).
   * Dynamic QR Code.
   * Clear instruction: *"Scan or Pay via Google Pay"*.
   * Transaction order reference.
3. The app saves this card to the secure app cache and dispatches an Android `ACTION_SEND` Intent with `image/png` and `FLAG_GRANT_READ_URI_PERMISSION`.
4. The system opens the Android Share Sheet highlighting Google Pay / UPI scanners. The user selects Google Pay, which automatically scans the card and opens the instant UPI payment window!

```kotlin
fun launchGooglePayShare(context: Context, qrData: String, amount: Double, orderId: String) {
    val voucherBitmap = createPaymentVoucherCard(context, qrData, amount, orderId)
    val imageUri = saveBitmapToCache(context, voucherBitmap, "payxmint_payment_card.png")

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_SUBJECT, "PayXMint UPI Payment - Order $orderId")
        putExtra(Intent.EXTRA_TEXT, "Pay ₹%.2f securely via Google Pay / UPI".format(amount))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Pay with Google Pay / UPI")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
```

---

## 8. Real-time Polling & Expiry Engine

When the checkout screen is displayed, start a coroutine polling loop that queries `POST /api/v1/check-status` every **3.5 seconds**:

```kotlin
fun startStatusPolling(orderId: String, totalTimeoutSeconds: Int = 900) {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
        var elapsed = 0
        val intervalMs = 3500L

        while (isActive && elapsed < totalTimeoutSeconds) {
            delay(intervalMs)
            elapsed += (intervalMs / 1000).toInt()

            val result = repository.checkStatus(orderId)
            result.onSuccess { response ->
                when (response.status.uppercase()) {
                    "SUCCESS" -> {
                        _paymentState.value = PaymentUiState.Success(
                            orderId = response.orderId,
                            amount = response.amount
                        )
                        return@launch
                    }
                    "EXPIRED" -> {
                        _paymentState.value = PaymentUiState.Expired(orderId = response.orderId)
                        return@launch
                    }
                    else -> {
                        // Keep polling while PENDING
                    }
                }
            }
        }

        // Timer elapsed
        _paymentState.value = PaymentUiState.Expired(orderId = orderId)
    }
}
```

---

## 9. Native Jetpack Compose UI Implementation

### 9.1 Single-Screen Layout Architecture

The entire checkout experience must fit on **a single screen without scrolling**. This is achieved by organizing the layout into 4 distinct vertical sections using `Arrangement.SpaceBetween`:

```
┌──────────────────────────────────────────────┐
│  [Header] ⚡ PayXMint · 100% secure           │
├──────────────────────────────────────────────┤
│  [Section 1: Amount & Timer]                 │
│      Amount to pay                           │
│      ₹1                                      │
│      ⏱ 14:46                                 │
│                                              │
│  [Section 2: Dynamic QR & Progress]          │
│      Scan QR to pay                          │
│      ┌────────────┐                          │
│      │  [QR Code] │                          │
│      └────────────┘                          │
│      Waiting for payment confirmation...     │
│                                              │
│  [Section 3: UPI App Buttons]                │
│      ────────── OR TAP BELOW ──────────      │
│      [ 🟣 PhonePe ]                          │
│      [ 🔵 G Pay   ]                          │
│      [ 🔷 paytm   ]                          │
│                                              │
│  [Section 4: Trust Footer]                   │
│      [🔒 256-bit SSL] [🛡 PCI-DSS] [✓ Verified]│
│      🔒 Powered by PayxMint                  │
└──────────────────────────────────────────────┘
```

### 9.2 Complete CheckoutScreen Composable

```kotlin
@Composable
fun ReadyCheckoutContent(
    intent: CreateIntentResponse,
    remainingSeconds: Int,
    isPolling: Boolean,
    onPhonePeClick: () -> Unit,
    onGooglePayClick: () -> Unit,
    onPaytmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Section 1: Amount & Timer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Amount to pay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = "₹${if (intent.amount % 1.0 == 0.0) intent.amount.toInt() else intent.amount}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFFBEB), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text("⏱ %02d:%02d".format(minutes, seconds), color = Color(0xFFB45309), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section 2: QR & Live Status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            QrCard(qrData = intent.qrData ?: intent.upiLink ?: "")
            Spacer(modifier = Modifier.height(6.dp))
            if (isPolling) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.5f).height(3.dp), color = Color(0xFF1E60FF))
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text("Waiting for payment confirmation...", fontSize = 12.sp, color = Color(0xFF64748B))
        }

        // Section 3: UPI App Buttons
        UpiAppButtons(
            onPhonePeClick = onPhonePeClick,
            onGooglePayClick = onGooglePayClick,
            onPaytmClick = onPaytmClick
        )

        // Section 4: Trust Badges
        TrustFooter()
    }
}
```

---

## 10. Production Security & Webhook Reconciliation

For test applications and rapid prototypes, making API requests directly from the Android app using an API key in `local.properties` is convenient.

However, for **production merchant deployments**, you must protect your PayXMint secret API key:

### 10.1 Recommended Production Architecture
1. **Mobile App** requests an order token from **Your Merchant Server**:
   ```
   Mobile App -> POST /api/create-order -> Merchant Backend
   ```
2. **Merchant Backend** calls PayXMint using the secret API key:
   ```
   Merchant Backend -> POST https://payxmint.com/api/v1/create-intent -> PayXMint
   ```
3. **Merchant Backend** returns `order_id`, `amount`, `qr_data`, and `upi_links` back to the mobile app.
4. **Mobile App** launches the native checkout UI.
5. **PayXMint Webhook** sends instant asynchronous confirmation to your backend:
   ```
   PayXMint Engine -> POST https://yourdomain.com/webhook/payxmint
   ```

### 10.2 Webhook Signature Verification (HMAC SHA-256)
Validate incoming webhooks using your webhook secret:

```kotlin
// Server-side Kotlin example
fun verifySignature(payload: String, signatureHeader: String, webhookSecret: String): Boolean {
    val sha256Hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(webhookSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
    sha256Hmac.init(secretKey)
    val hash = sha256Hmac.doFinal(payload.toByteArray(Charsets.UTF_8))
    val expectedSignature = hash.joinToString("") { "%02x".format(it) }
    return MessageDigest.isEqual(expectedSignature.toByteArray(), signatureHeader.toByteArray())
}
```

---

## 11. Troubleshooting & Common Pitfalls

| Symptom / Error | Root Cause | Solution |
| :--- | :--- | :--- |
| **`503 ROUTING_ERROR: No available VPAs`** | Passing `"gateway": "GPAY"` in `CreateIntentRequest`. | Omit the `"gateway"` key or set it to `null`. The smart router automatically selects active VPAs. |
| **`400 Bad Request`** on create-intent | `order_id` contains hyphens (`-`), underscores (`_`), or is `< 8` chars. | Use strictly alphanumeric string: `^[a-zA-Z0-9]+$` with length `>= 8`. |
| **`FileUriExposedException`** on Android 7.0+ | Attempting to share image URI using `file://` scheme. | Use `FileProvider.getUriForFile()` with `content://` scheme and grant read permissions. |
| **`ActivityNotFoundException`** | Target UPI app is not installed on user's device. | Catch exception, show friendly Toast, or fallback to the generic `upi://pay` intent. |
| **UPI Apps Not Detected (Android 11+)** | Missing `<queries>` declaration in `AndroidManifest.xml`. | Add package names (`com.phonepe.app`, `net.one97.paytm`, etc.) to `<queries>`. |

---

## 12. Kotlin SDK Architecture & Publishing Proposal

To allow merchants to integrate PayXMint into their apps with **a single line of code**, we propose distributing the core checkout engine as a pre-packaged Android Library:

### 12.1 Implementation Options

#### Option A: JitPack (Fastest & Zero Setup)
* **How it works**: Tag a GitHub release (e.g. `v1.0.0`) on `https://github.com/8nc0x/payxmint-apk`.
* **Merchant installation**:
  ```kotlin
  // settings.gradle.kts
  dependencyResolutionManagement {
      repositories {
          maven { url = uri("https://jitpack.io") }
      }
  }

  // app/build.gradle.kts
  dependencies {
      implementation("com.github.8nc0x:payxmint-apk:1.0.0")
  }
  ```

#### Option B: Maven Central / Sonatype (Official Industry Standard)
* **How it works**: Register the `com.payxmint` namespace on the Sonatype Central Portal, sign artifacts with GPG, and publish using Gradle `maven-publish`.
* **Merchant installation**:
  ```kotlin
  dependencies {
      implementation("com.payxmint:android-sdk:1.0.0")
  }
  ```

#### Option C: Direct AAR Distribution
* Run `./gradlew :payxmint-sdk:assembleRelease` to generate `payxmint-sdk-release.aar`.
* Merchants drop this file into their `app/libs/` directory.

### 12.2 What the Merchant's Integration Looks Like with the SDK
Instead of copying 10+ Kotlin files, a merchant only writes:

```kotlin
PayxmintCheckout(
    amount = 50.0,
    apiKey = "YOUR_API_KEY",
    onSuccess = { orderId, amount ->
        // Handle successful payment
    },
    onFailure = { error ->
        // Handle failure
    }
)
```

---

## 13. Reference App & Source Code Package

To inspect the working application, you can use either of the following resources:

1. **GitHub Repository**:
   Clone the active source code:
   ```bash
   git clone https://github.com/8nc0x/payxmint-apk.git
   ```

2. **Clean Reference Source ZIP**:
   A scrubbed archive containing the entire Android Studio project (excluding build directories and credentials) is provided directly in the root directory:
   * **Filename**: `payxmint-android-reference.zip`
   * **Unzip & Open**: Extract and open directly in Android Studio Ladybug / Meerkat.

3. **Pre-compiled Testing APK**:
   * **Filename**: `PayXMint.apk` (8.88 MB)
   * Install directly on any Android device running Android 7.0 (API 24) or higher.
