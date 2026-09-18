package com.payxmint.myapp.data.api

import com.payxmint.myapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://payxmint.com/api/v1/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val apiKey = BuildConfig.PAYXMINT_API_KEY

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")

        if (apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE") {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: PayxmintApiService = retrofit.create(PayxmintApiService::class.java)
}
