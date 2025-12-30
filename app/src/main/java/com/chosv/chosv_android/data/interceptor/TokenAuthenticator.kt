package com.chosv.chosv_android.data.interceptor

import android.util.Log
import com.chosv.chosv_android.data.network.AuthApiService
import com.chosv.chosv_android.preferences.TokenPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator để tự động refresh token khi nhận được 401 Unauthorized.
 * Sẽ gọi API /api/User/refreshToken và cập nhật access token mới.
 */
class TokenAuthenticator(
    private val tokenPreferences: TokenPreferences,
    private val authApiServiceProvider: () -> AuthApiService
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "Received 401 for ${response.request.url}, attempting to refresh token")

        // Tránh vòng lặp vô hạn - nếu đã retry quá nhiều lần thì dừng
        if (responseCount(response) >= 3) {
            Log.e(TAG, "Too many retry attempts, giving up")
            return null
        }

        // Không refresh token cho chính API refreshToken
        if (response.request.url.encodedPath.contains("refreshToken")) {
            Log.d(TAG, "RefreshToken API failed, not retrying")
            return null
        }

        return runBlocking {
            try {
                val authApiService = authApiServiceProvider()
                val refreshResponse = authApiService.refreshToken()

                if (refreshResponse.isSuccessful) {
                    val newAccessToken = refreshResponse.body()
                    if (!newAccessToken.isNullOrBlank()) {
                        Log.d(TAG, "Token refreshed successfully")
                        // Lưu access token mới
                        tokenPreferences.updateAccessToken(newAccessToken)

                        // Retry request với token mới
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    }
                }

                Log.e(TAG, "Failed to refresh token: ${refreshResponse.code()}")
                // Xóa tokens nếu refresh thất bại
                tokenPreferences.clearAllTokens()
                null
            } catch (e: Exception) {
                Log.e(TAG, "Exception while refreshing token", e)
                tokenPreferences.clearAllTokens()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

