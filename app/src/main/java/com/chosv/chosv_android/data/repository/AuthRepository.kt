package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.*
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.preferences.TokenPreferences

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
}

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenPreferences: TokenPreferences // Injected TokenPreferences
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResponse {
        val response = authApiService.login(request)
        // Save both tokens after a successful login
        tokenPreferences.updateAccessToken(response.accessToken)
        tokenPreferences.updateRefreshToken(response.refreshToken)
        Log.d("AuthRepository", "Login successful, tokens saved: $response")
        return response
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return authApiService.register(request)
    }
}