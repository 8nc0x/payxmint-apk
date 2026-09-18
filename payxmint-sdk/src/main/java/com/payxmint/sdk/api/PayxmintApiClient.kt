package com.payxmint.sdk.api

import com.payxmint.sdk.model.CheckStatusRequest
import com.payxmint.sdk.model.CheckStatusResponse
import com.payxmint.sdk.model.CreateIntentRequest
import com.payxmint.sdk.model.CreateIntentResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface PayxmintService {
    @POST("create-intent")
    suspend fun createIntent(
        @Header("Authorization") authorization: String,
        @Body request: CreateIntentRequest
    ): Response<CreateIntentResponse>

    @POST("check-status")
    suspend fun checkStatus(
        @Header("Authorization") authorization: String,
        @Body request: CheckStatusRequest
    ): Response<CheckStatusResponse>
}

object PayxmintApiClient {
    private const val BASE_URL = "https://payxmint.com/api/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: PayxmintService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PayxmintService::class.java)
    }
}
