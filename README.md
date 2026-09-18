# PayXMint — Native Android Headless Custom Checkout

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

An official reference Android implementation of the **PayXMint Automated UPI Payment Gateway**, demonstrating a 100% native, headless custom checkout flow built with **Jetpack Compose** and **Material 3**.

---

## ⚡ Key Highlights

* **100% Headless Native Experience**: Zero WebViews. Never redirects to `checkout_url`. Eliminates browser redirects and app drops.
* **Dynamic ZXing QR Code**: Generates crisp, real-time UPI QR codes directly on-device using the `qr_data` returned from the PayXMint API.
* **Instant One-Tap UPI Intents**:
  * **PhonePe**: Deep-links directly via `phonepe://` scheme.
  * **Paytm**: Deep-links directly via `paytmmp://` scheme.
  * **Google Pay**: Dispatches a 1000×800px (5:4) payment voucher card via Android's native share sheet and `FileProvider`.
* **Single-Screen, Non-Scrollable Layout**: Precision UI tuned to fit 100% on any screen height without vertical scrolling.
* **Automated Real-Time Polling**: Coroutine polling loop querying `POST /api/v1/check-status` every 3.5 seconds with live visual progress and a 15-minute countdown timer.
* **Amount Input Screen**: Interactive test screen allowing arbitrary payment amounts (with ₹1, ₹2, ₹5, ₹10 quick presets).

---

## 📖 Merchant Integration Documentation

For complete, step-by-step developer instructions on how to integrate this custom checkout into your own apps, refer to:

👉 **[PAYXMINT_ANDROID_INTEGRATION_GUIDE.md](PAYXMINT_ANDROID_INTEGRATION_GUIDE.md)**

---

## 📦 Project Artifacts & Downloads

* **Pre-compiled Testing APK**: [`PayXMint.apk`](PayXMint.apk) (8.88 MB) — Ready to install on any Android phone (API 24+).
* **Reference Source Archive**: [`payxmint-android-reference.zip`](payxmint-android-reference.zip) — Complete project bundle for offline inspection.
* **GitHub Repository**: [https://github.com/8nc0x/payxmint-apk](https://github.com/8nc0x/payxmint-apk)

---

## 🚀 Quick Start (Running Locally)

### 1. Clone the Repository
```bash
git clone https://github.com/8nc0x/payxmint-apk.git
cd payxmint-apk
```

### 2. Configure API Credentials
Add your secret API key in `local.properties` (this file is excluded from Git):
```properties
payxmint.api.key=YOUR_SECRET_API_KEY
payxmint.webhook.secret=YOUR_WEBHOOK_SECRET
```

### 3. Build & Install
With your Android device connected via USB/WiFi:
```bash
# Set Java 17/21 home if needed
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Build debug APK and install
.\gradlew.bat installDebug
```

Or assemble a shareable release APK:
```bash
.\gradlew.bat assembleRelease
```
The resulting APK will be placed at `app/build/outputs/apk/release/app-release.apk`.

---

## 🏗 Architecture & Project Structure

```
payxmint/
├── PAYXMINT_ANDROID_INTEGRATION_GUIDE.md  # Complete merchant documentation
├── PayXMint.apk                           # Shareable testing APK
├── app/
│   └── src/main/java/com/payxmint/myapp/
│       ├── MainActivity.kt                # Single-activity container
│       ├── PayxmintApp.kt                 # Compose navigation graph
│       ├── data/
│       │   ├── api/PayxmintApiService.kt  # Retrofit API interface
│       │   ├── model/PayxmintModels.kt    # Data contracts & DTOs
│       │   └── repository/                # PaymentRepository implementation
│       ├── domain/
│       │   └── PaymentUiState.kt          # Sealed UI state machine
│       ├── ui/
│       │   ├── checkout/
│       │   │   ├── CheckoutScreen.kt      # Single-screen headless checkout
│       │   │   ├── CheckoutViewModel.kt   # Polling loop & intent lifecycle
│       │   │   └── components/
│       │   │       ├── CheckoutHeader.kt  # Branded top bar
│       │   │       ├── QrCard.kt          # ZXing dynamic QR with brackets
│       │   │       ├── UpiAppButtons.kt   # PhonePe, GPay, Paytm buttons
│       │   │       └── TrustFooter.kt     # SSL & verification badges
│       │   ├── shop/
│       │   │   └── AmountInputScreen.kt   # Amount input & quick presets
│       │   ├── result/
│       │   │   └── PaymentResultScreen.kt # Success / Expired confirmation
│       │   └── util/
│       │       ├── QrCodeGenerator.kt     # ZXing bitmap generator
│       │       └── IntentLauncher.kt      # UPI launchers & Google Pay card
│       └── res/xml/file_paths.xml         # FileProvider cache paths
└── payxmint-sdk/                          # Reusable Android SDK library module
```

---

## 🔒 Security Best Practices

1. **Credentials Out of Version Control**: `local.properties` is strictly ignored by Git. Secrets are injected at build time via `BuildConfig.PAYXMINT_API_KEY`.
2. **Server-Side Intent Creation**: In production environments, payment intents should be created via your backend server to avoid exposing your API key inside mobile client binaries.
3. **Webhook Verification**: Reconcile order statuses securely on your server using HMAC SHA-256 webhook signatures.
