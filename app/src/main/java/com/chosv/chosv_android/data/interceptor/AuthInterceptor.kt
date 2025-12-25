package com.chosv.chosv_android.data.interceptor

import android.util.Log
import com.chosv.chosv_android.preferences.TokenPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenPreferences: TokenPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = runBlocking { tokenPreferences.accessTokenFlow.firstOrNull() }
        val request = token
            ?.takeIf { it.isNotBlank() }
            ?.let {
                original.newBuilder()
                    .header("Authorization", "Bearer $it")
                    .build()
            } ?: original
        Log.d("HTTP", "Request URL = ${chain.request().url}")
        return chain.proceed(request)
    }
}