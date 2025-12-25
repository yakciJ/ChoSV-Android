package com.chosv.chosv_android.data

import android.content.Context
import com.chosv.chosv_android.data.interceptor.AuthInterceptor
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.data.repository.AuthRepository
import com.chosv.chosv_android.data.repository.AuthRepositoryImpl
import com.chosv.chosv_android.preferences.TokenPreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object EnvVariable {
    const val OLD_HOST_BACKEND = "https://localhost:7049/"
    const val BASE_URL = "http://10.0.2.2:5092/"
}

interface AppContainer {
    val tokenPreferences: TokenPreferences
    val authRepository: AuthRepository
}

class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val tokenPreferences by lazy {
        TokenPreferences(context)
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val authInterceptor by lazy {
        AuthInterceptor(tokenPreferences)
    }

    private val tokenRefreshOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val tokenRefreshRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(EnvVariable.BASE_URL)
        .client(tokenRefreshOkHttpClient) // Use Alice here
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val tokenRefreshApiService: AuthApiService by lazy {
        tokenRefreshRetrofit.create(AuthApiService::class.java)
    }
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // This part stays the same. It uses the updated "Bob".
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(EnvVariable.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, tokenPreferences)
    }

}