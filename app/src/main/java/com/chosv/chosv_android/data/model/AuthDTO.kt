package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val userName: String,
    val password: String,
    val rememberMe: Boolean = true
)

@Serializable
data class RegisterRequest(
    val userName: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val userId: String,
    val userName: String,
    val email: String,
    val fullName: String,
    val avatarImage: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String = "" // Có thể rỗng vì BE gửi qua cookie
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UserProfile(
    val userId: String,
    val userName: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
    val avatarImage: String? = null,
    val bio: String? = null,
    val address: String? = null,
    val createdAt: String? = null
)
