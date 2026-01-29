package com.chosv.chosv_android.data.repository

import android.util.Log
import com.chosv.chosv_android.data.model.University
import com.chosv.chosv_android.data.network.AuthApiService

interface UniversityRepository {
    /**
     * Lấy danh sách tất cả trường đại học
     */
    suspend fun getUniversities(): Result<List<University>>
}

class UniversityRepositoryImpl(
    private val authApiService: AuthApiService
) : UniversityRepository {

    companion object {
        private const val TAG = "UniversityRepository"
    }

    override suspend fun getUniversities(): Result<List<University>> {
        return try {
            val response = authApiService.getUniversities()
            if (response.isSuccessful) {
                val universities = response.body()?.items ?: emptyList()
                Log.d(TAG, "Loaded ${universities.size} universities")
                Result.success(universities)
            } else {
                Log.e(TAG, "Failed to load universities: ${response.code()}")
                Result.failure(Exception("Không thể tải danh sách trường đại học"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading universities", e)
            Result.failure(e)
        }
    }
}
