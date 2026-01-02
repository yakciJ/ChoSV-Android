package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.ChangePasswordRequest
import com.chosv.chosv_android.data.model.UpdateProfileRequest
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
     * Đăng xuất - xóa tất cả tokens và gọi API logout
     */
    suspend fun logout(): Result<String>

    /**
     * Cập nhật thông tin cá nhân
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<String>

    /**
     * Cập nhật avatar
     */
    suspend fun updateAvatar(imageUrl: String): Result<String>

    /**
     * Đổi mật khẩu
     */
    suspend fun changePassword(request: ChangePasswordRequest): Result<String>

    /**
     * Quên mật khẩu
     */
    suspend fun forgotPassword(email: String): Result<String>
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

    override suspend fun logout(): Result<String> {
        return try {
            val response = authApiService.logout()
            // Luôn xóa tokens dù API thành công hay thất bại
            tokenPreferences.clearAllTokens()
            if (response.isSuccessful) {
                Log.d(TAG, "Logout successful")
                Result.success(response.body()?.message ?: "Đăng xuất thành công!")
            } else {
                Log.e(TAG, "Logout API failed: ${response.code()}")
                Result.success("Đăng xuất thành công!") // Vẫn thành công vì đã xóa local tokens
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during logout", e)
            tokenPreferences.clearAllTokens() // Vẫn xóa tokens
            Result.success("Đăng xuất thành công!")
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<String> {
        return try {
            val response = authApiService.updateProfile(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Update profile successful")
                Result.success(response.body()?.message ?: "Cập nhật thông tin thành công!")
            } else {
                Log.e(TAG, "Update profile failed: ${response.code()}")
                Result.failure(Exception("Cập nhật thông tin thất bại"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating profile", e)
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(imageUrl: String): Result<String> {
        return try {
            val response = authApiService.updateAvatar(imageUrl)
            if (response.isSuccessful) {
                Log.d(TAG, "Update avatar successful")
                Result.success(response.body()?.message ?: "Đổi ảnh đại diện thành công!")
            } else {
                Log.e(TAG, "Update avatar failed: ${response.code()}")
                Result.failure(Exception("Đổi ảnh đại diện thất bại"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating avatar", e)
            Result.failure(e)
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): Result<String> {
        return try {
            val response = authApiService.changePassword(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Change password successful")
                Result.success(response.body()?.message ?: "Đổi mật khẩu thành công!")
            } else {
                Log.e(TAG, "Change password failed: ${response.code()}")
                Result.failure(Exception("Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception changing password", e)
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApiService.forgotPassword(email)
            if (response.isSuccessful) {
                Log.d(TAG, "Forgot password request successful")
                Result.success(response.body()?.message ?: "Nếu email tồn tại, link đặt lại mật khẩu đã được gửi!")
            } else {
                Log.e(TAG, "Forgot password failed: ${response.code()}")
                Result.failure(Exception("Yêu cầu không thành công"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting forgot password", e)
            Result.failure(e)
        }
    }
}

