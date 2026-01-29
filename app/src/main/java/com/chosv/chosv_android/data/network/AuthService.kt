package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthApiService {
    @POST("api/User/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/User/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterSuccessResponse>

    // API refresh token - trả về plain string (access token mới)
    @GET("api/User/refreshToken")
    suspend fun refreshToken(): Response<String>

    // API lấy thông tin user hiện tại
    @GET("api/User/me")
    suspend fun getMe(): Response<UserProfile>

    // API đăng xuất - revoke refresh token
    @POST("api/User/logout")
    suspend fun logout(): Response<MessageResponse>

    // API cập nhật thông tin cá nhân
    @PUT("api/User/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<MessageResponse>

    // API cập nhật avatar
    @PUT("api/User/avatar")
    suspend fun updateAvatar(@Query("imageUrl") imageUrl: String): Response<MessageResponse>

    // API đổi mật khẩu
    @PUT("api/User/changePassword")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    // API quên mật khẩu
    @POST("api/User/forgotPassword")
    suspend fun forgotPassword(@Query("email") email: String): Response<MessageResponse>

    // API lấy danh sách trường đại học
    @GET("api/University")
    suspend fun getUniversities(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): Response<UniversityResponse>
}