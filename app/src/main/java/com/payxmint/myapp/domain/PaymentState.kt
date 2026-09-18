package com.payxmint.myapp.domain

import com.payxmint.myapp.data.model.CreateIntentResponse
import com.payxmint.myapp.data.model.PayerInfo
import com.payxmint.myapp.data.model.SettlementInfo

sealed interface PaymentUiState {
    object Idle : PaymentUiState

    data class CreatingIntent(
        val amount: Double
    ) : PaymentUiState

    data class Ready(
        val intent: CreateIntentResponse,
        val remainingSeconds: Int = 900, // 15 minutes default timer
        val isPolling: Boolean = true
    ) : PaymentUiState

    data class Success(
        val orderId: String,
        val amount: Double,
        val payer: PayerInfo? = null,
        val settlement: SettlementInfo? = null
    ) : PaymentUiState

    data class Expired(
        val orderId: String
    ) : PaymentUiState

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : PaymentUiState
}
