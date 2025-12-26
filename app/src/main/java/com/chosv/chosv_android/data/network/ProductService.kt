package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface ProductApiService {

    /**
     * Lấy danh sách các sản phẩm mới nhất, có phân trang
     * @param page: số trang hiện tại
     * @param pageSize: số lượng sản phẩm trên một trang
     */
    @GET("api/Product/newest")
    suspend fun getNewestProducts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 12
    ): PaginatedResponse<Product>

    /**
     * Lấy danh sách các sản phẩm phổ biến, có phân trang
     * @param page: số trang hiện tại
     * @param pageSize: số lượng sản phẩm trên một trang
     * @param daysBack: số ngày quay lại để tính độ phổ biến
     */
    @GET("api/Product/popular")
    suspend fun getPopularProducts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 12,
        @Query("daysBack") daysBack: Int = 30
    ): PaginatedResponse<Product>

    @GET("api/Product/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: Int
    ): ProductDetail
}