package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.UserProfile
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.preferences.TokenPreferences
import kotlinx.coroutines.flow.firstOrNull

interface UserRepository {
    /**
     * Lấy thông tin user hiện tại.
     * @return UserProfile nếu thành công, null nếu không authenticated hoặc lỗi
     */
    suspend fun getCurrentUser(): UserProfile?

    /**
     * Kiểm tra xem user đã đăng nhập chưa bằng cách gọi API /me
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Đăng xuất - xóa tất cả tokens
     */
    suspend fun logout()
}

class UserRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenPreferences: TokenPreferences
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    override suspend fun getCurrentUser(): UserProfile? {
        return try {
            // Kiểm tra xem có access token không
            val accessToken = tokenPreferences.accessTokenFlow.firstOrNull()
            if (accessToken.isNullOrBlank()) {
                Log.d(TAG, "No access token found")
                return null
            }

            val response = authApiService.getMe()
            if (response.isSuccessful) {
                Log.d(TAG, "Get current user successful")
                response.body()
            } else {
                Log.e(TAG, "Get current user failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting current user", e)
            null
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    override suspend fun logout() {
        tokenPreferences.clearAllTokens()
        Log.d(TAG, "User logged out, tokens cleared")
    }
}

