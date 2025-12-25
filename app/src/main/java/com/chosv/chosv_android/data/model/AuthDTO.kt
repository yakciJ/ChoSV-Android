package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val userName: String,
    val password: String
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
    val refreshToken: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UserProfile(
    val id: String,
    val userName: String,
    val email: String
)
