package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.AuthResponse
import com.chosv.chosv_android.data.model.LoginRequest
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.RefreshTokenRequest
import com.chosv.chosv_android.data.model.RegisterRequest
import com.chosv.chosv_android.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteApiService {
    // Favorite APIs
    @GET("api/Favorite")
    suspend fun getFavorites(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PaginatedResponse<Product>

    @POST("api/Favorite")
    suspend fun addFavorite(@Query("productId") productId: String)

    @DELETE("api/Favorite/{id}")
    suspend fun deleteFavorite(@Path("id") productId: String)
}
