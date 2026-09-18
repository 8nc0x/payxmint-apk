# PayXMint Android Development Rules

Permanent development guidelines and constraints for the PayXMint native Android project.

---

## 1. Platform & Technology Stack
1. **Native Android Application**: This project is exclusively a native Android application.
2. **Kotlin & Jetpack Compose**: All code must be written in Kotlin, and all UI must be built with Jetpack Compose.
3. **Material 3**: Use Jetpack Compose Material 3 components, styling, and typography.
4. **No XML Layouts**: Do not use legacy XML layouts or View system bindings for UI.
5. **Native Custom Checkout**: The checkout flow must be implemented entirely in native Jetpack Compose UI.
6. **No WebView for Checkout**: Never use `WebView` to render the checkout flow.
7. **No Checkout URL Display**: Never redirect to or display `checkout_url`. This project implements a true headless custom checkout experience.

---

## 2. Branding & Test Products
8. **Brand Identity**: The permanent brand name is **PayXMint**. Never use "Mahi" or any deprecated naming.
9. **Test Product Catalog**:
   - Initial test products are strictly:
     - **Apple**: ₹1
     - **Orange**: ₹2
   - Product catalog prices may be hardcoded solely as static test-product definitions.

---

## 3. Payment Data & API Integrity
10. **Zero Hardcoded Payment Data**: Payment information must **NEVER** be hardcoded.
11. **Dynamic Transaction Data**:
    - `amount`, `order_id`, `payment_token`, `qr_data`, `upi_links`, `status`, payer information, expiry information, and all other transaction data **must** originate directly from live PayXMint API responses.
    - Never hardcode sample API response payloads or mock values from documentation.
12. **Native QR Code Generation**: Generate the payment QR code dynamically using the `qr_data` returned by the PayXMint API.
13. **Dynamic UPI Intents**: Invoke UPI payment applications dynamically using the `upi_links` provided in the API response.
14. **Google Pay Integration**: The Google Pay flow must strictly and accurately follow the official PayXMint documentation.
15. **Documentation Fidelity**:
    - Always inspect the supplied PayXMint documentation and official Android documentation before implementing unfamiliar APIs or Android behaviors.
    - **Do not invent** undocumented API fields, endpoints, request formats, or behaviors.
    - If documentation is missing or ambiguous on any detail, **stop and clearly state the missing information** instead of assuming or making up specifications.

---

## 4. Security & Secret Management
16. **Credentials Out of Source Control**: Never commit API keys, webhook secrets, tokens, passwords, private keys, or any other credentials to version control.
17. **Configurable Secrets**: Keep secrets configurable through local development configuration (e.g., `local.properties`, ignored config files, or environment variables).

---

## 5. Architecture & Code Quality
18. **Intentionally Small & Focused**: Keep the application lean, concise, and focused strictly on the required functionality.
19. **No Over-Engineering**: Do not introduce unnecessary libraries, complex multi-layer abstractions, or heavyweight architectural overhead.
20. **Modular & Testable**: Prefer small, single-responsibility, testable Kotlin files over large monolithic files.
21. **No Unrelated Changes**: Keep commits and modifications scoped strictly to the task at hand.
22. **Continuous Verification**: After every meaningful implementation step, build the project and fix any compilation errors before continuing.
