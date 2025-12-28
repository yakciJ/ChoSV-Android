package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.network.FavoriteApiService

interface FavoriteRepository {
    suspend fun getFavorites(page: Int, pageSize: Int): PaginatedResponse<Product>
    suspend fun addFavorite(productId: String)
    suspend fun deleteFavorite(productId: String)
}

class FavoriteRepositoryImpl(
    private val favoriteApiService: FavoriteApiService
) : FavoriteRepository {
    override suspend fun getFavorites(page: Int, pageSize: Int): PaginatedResponse<Product> {
        return favoriteApiService.getFavorites(page, pageSize)
    }

    override suspend fun addFavorite(productId: String) {
        favoriteApiService.addFavorite(productId)
    }

    override suspend fun deleteFavorite(productId: String) {
        favoriteApiService.deleteFavorite(productId)
    }
}
