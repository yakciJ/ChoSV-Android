package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.*
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.preferences.TokenPreferences
import kotlinx.serialization.json.Json

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<String>
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

    override suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = authApiService.register(request)
            if (response.isSuccessful) {
                val successResponse = response.body()!!
                Log.d("AuthRepository", "Register successful: ${successResponse.message}")
                Result.success(successResponse.message)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Register failed: ${response.code()}, body: $errorBody")

                // Parse error response
                val errorMessage = try {
                    val json = Json { ignoreUnknownKeys = true }
                    val errorResponse = json.decodeFromString<RegisterErrorResponse>(errorBody ?: "")
                    parseRegisterError(errorResponse.error)
                } catch (e: Exception) {
                    "Đăng ký thất bại"
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register exception", e)
            Result.failure(Exception("Đăng ký thất bại: ${e.localizedMessage}"))
        }
    }

    private fun parseRegisterError(error: String): String {
        return when (error) {
            "DuplicateUserName" -> "Tên tài khoản đã tồn tại!"
            "InvalidUserName" -> "Tên tài khoản không hợp lệ!"
            "PasswordTooShort" -> "Mật khẩu quá ngắn!"
            "PasswordRequiresNonAlphanumeric" -> "Mật khẩu thiếu ký tự đặc biệt!"
            "PasswordRequiresLower" -> "Mật khẩu phải gồm chữ thường!"
            "PasswordRequiresUpper" -> "Mật khẩu phải gồm chữ viết hoa!"
            "PasswordRequiresDigit" -> "Mật khẩu phải gồm số!"
            "DuplicateEmail" -> "Email đã tồn tại!"
            "Invalid email format" -> "Email không hợp lệ!"
            else -> "Đã có lỗi không xác định xảy ra!"
        }
    }
}