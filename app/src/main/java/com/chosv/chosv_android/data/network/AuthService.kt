package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/User/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/User/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // API refresh token - trả về plain string (access token mới)
    @GET("api/User/refreshToken")
    suspend fun refreshToken(): Response<String>

    // API lấy thông tin user hiện tại
    @GET("api/User/me")
    suspend fun getMe(): Response<UserProfile>
}