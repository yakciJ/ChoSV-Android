package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.*
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.preferences.TokenPreferences

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): AuthResponse
}

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenPreferences: TokenPreferences // Injected TokenPreferences
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = authApiService.login(request)
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                // Lưu access token sau khi login thành công
                tokenPreferences.updateAccessToken(authResponse.accessToken)
                // RefreshToken sẽ được gửi qua cookie và lưu bởi CookieJar
                Log.d("AuthRepository", "Login successful, access token saved")
                Result.success(authResponse)
            } else {
                Log.e("AuthRepository", "Login failed: ${response.code()}")
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception", e)
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return authApiService.register(request)
    }
}