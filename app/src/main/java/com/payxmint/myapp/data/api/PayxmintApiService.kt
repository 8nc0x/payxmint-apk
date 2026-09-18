package com.payxmint.myapp.data.api

import com.payxmint.myapp.data.model.CheckStatusRequest
import com.payxmint.myapp.data.model.CheckStatusResponse
import com.payxmint.myapp.data.model.CreateIntentRequest
import com.payxmint.myapp.data.model.CreateIntentResponse
import com.payxmint.myapp.data.model.SimulatePaymentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PayxmintApiService {

    @POST("create-intent")
    suspend fun createIntent(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateIntentRequest
    ): Response<CreateIntentResponse>

    @POST("check-status")
    suspend fun checkStatus(
        @Body request: CheckStatusRequest
    ): Response<CheckStatusResponse>

    @POST("simulate-payment")
    suspend fun simulatePayment(
        @Body request: SimulatePaymentRequest
    ): Response<Map<String, Any>>
}
