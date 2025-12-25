package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/User/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/User/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/User/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @GET("api/User/me")
    suspend fun getProfile(): UserProfile
}