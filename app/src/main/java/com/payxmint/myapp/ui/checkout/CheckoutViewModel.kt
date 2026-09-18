package com.payxmint.myapp.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payxmint.myapp.data.api.ApiClient
import com.payxmint.myapp.data.model.CheckStatusRequest
import com.payxmint.myapp.data.model.CreateIntentRequest
import com.payxmint.myapp.data.model.CreateIntentResponse
import com.payxmint.myapp.domain.PaymentUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class CheckoutViewModel : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val paymentState: StateFlow<PaymentUiState> = _paymentState.asStateFlow()

    private var pollingJob: Job? = null
    private var timerJob: Job? = null

    fun initiatePayment(amount: Double) {
        if (amount <= 0.0) {
            _paymentState.value = PaymentUiState.Error("Invalid order amount: ₹$amount")
            return
        }

        stopPolling()
        _paymentState.value = PaymentUiState.CreatingIntent(amount)

        viewModelScope.launch {
            try {
                val randomSuffix = UUID.randomUUID().toString().filter { it.isLetterOrDigit() }.take(6)
                val orderId = "ord" + System.currentTimeMillis() + randomSuffix
                val idempotencyKey = UUID.randomUUID().toString()
                val formattedAmount = String.format(Locale.US, "%.2f", amount)

                val request = CreateIntentRequest(
                    amount = formattedAmount,
                    orderId = orderId,
                    customerMobile = "9876543210",
                    customerEmail = "test@payxmint.com",
                    redirectUrl = "https://payxmint.com/thankyou",
                    gateway = "GPAY"
                )

                val response = ApiClient.apiService.createIntent(
                    idempotencyKey = idempotencyKey,
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    val intentResponse = response.body()!!
                    _paymentState.value = PaymentUiState.Ready(
                        intent = intentResponse,
                        remainingSeconds = 900, // 15 minutes
                        isPolling = true
                    )
                    startStatusPolling(intentResponse.orderId, intentResponse)
                    startCountdownTimer()
                } else {
                    val rawError = response.errorBody()?.string().orEmpty()
                    val userMessage = try {
                        val json = com.google.gson.JsonParser.parseString(rawError).asJsonObject
                        if (json.has("error")) {
                            val errObj = json.get("error")
                            if (errObj.isJsonObject && errObj.asJsonObject.has("message")) {
                                errObj.asJsonObject.get("message").asString
                            } else if (errObj.isJsonPrimitive) {
                                errObj.asString
                            } else {
                                rawError
                            }
                        } else rawError
                    } catch (_: Exception) {
                        rawError
                    }
                    _paymentState.value = PaymentUiState.Error(
                        message = "PayXMint (${response.code()}): $userMessage"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _paymentState.value = PaymentUiState.Error(
                    message = e.localizedMessage ?: "Network error connecting to PayXMint"
                )
            }

        }
    }

    private fun startStatusPolling(orderId: String, currentIntent: CreateIntentResponse) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3500) // Poll approximately every 3.5 seconds per documentation

                try {
                    val response = ApiClient.apiService.checkStatus(CheckStatusRequest(orderId = orderId))
                    if (response.isSuccessful && response.body() != null) {
                        val statusData = response.body()!!
                        when (statusData.status.uppercase(Locale.ROOT)) {
                            "SUCCESS" -> {
                                stopPolling()
                                _paymentState.value = PaymentUiState.Success(
                                    orderId = orderId,
                                    amount = statusData.amount ?: currentIntent.amount,
                                    payer = statusData.payer,
                                    settlement = statusData.settlement
                                )
                                break
                            }
                            "EXPIRED" -> {
                                stopPolling()
                                _paymentState.value = PaymentUiState.Expired(orderId = orderId)
                                break
                            }
                            else -> {
                                // PENDING - keep polling
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Non-fatal network glitch during polling - keep retrying
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentState = _paymentState.value
                if (currentState is PaymentUiState.Ready) {
                    val nextSeconds = currentState.remainingSeconds - 1
                    if (nextSeconds <= 0) {
                        stopPolling()
                        _paymentState.value = PaymentUiState.Expired(currentState.intent.orderId)
                        break
                    } else {
                        _paymentState.value = currentState.copy(remainingSeconds = nextSeconds)
                    }
                } else {
                    break
                }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        timerJob?.cancel()
        timerJob = null
    }

    fun resetState() {
        stopPolling()
        _paymentState.value = PaymentUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
