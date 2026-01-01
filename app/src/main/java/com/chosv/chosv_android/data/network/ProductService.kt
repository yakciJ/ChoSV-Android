package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.CreateProductRequest
import com.chosv.chosv_android.data.model.CreateProductResponse
import com.chosv.chosv_android.data.model.MessageResponse
import com.chosv.chosv_android.data.model.MyProduct
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.model.UpdateProductRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path

interface ProductApiService {

    /**
     * Lấy danh sách các sản phẩm mới nhất, có phân trang
     */
    @GET("api/Product/newest")
    suspend fun getNewestProducts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 12
    ): PaginatedResponse<Product>

    /**
     * Lấy danh sách các sản phẩm phổ biến, có phân trang
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

    /**
     * Tạo sản phẩm mới
     */
    @POST("api/Product")
    suspend fun createProduct(
        @Body request: CreateProductRequest
    ): CreateProductResponse

    /**
     * Lấy danh sách sản phẩm của người dùng hiện tại
     * @param page: số trang
     * @param pageSize: số lượng mỗi trang
     * @param status: lọc theo trạng thái (Pending, Approved, Rejected, Sold) - null để lấy tất cả
     */
    @GET("api/Product/me")
    suspend fun getMyProducts(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("status") status: String? = null
    ): PaginatedResponse<MyProduct>

    /**
     * Cập nhật thông tin sản phẩm
     */
    @PUT("api/Product/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: Int,
        @Body request: UpdateProductRequest
    ): MessageResponse

    /**
     * Cập nhật trạng thái sản phẩm
     * @param productId: ID sản phẩm
     * @param status: trạng thái mới (Approved, Sold)
     */
    @PUT("api/Product/status/{productId}")
    suspend fun updateProductStatus(
        @Path("productId") productId: Int,
        @Body status: String
    ): MessageResponse

    /**
     * Xóa sản phẩm của người dùng
     */
    @DELETE("api/Product/user/{productId}")
    suspend fun deleteProduct(
        @Path("productId") productId: Int
    ): MessageResponse

    /**
     * Tìm kiếm sản phẩm
     * @param search: từ khóa tìm kiếm (bắt buộc)
     * @param categoryId: lọc theo danh mục (tùy chọn)
     * @param minPrice: giá tối thiểu (tùy chọn)
     * @param maxPrice: giá tối đa (tùy chọn)
     * @param page: số trang
     * @param pageSize: số lượng mỗi trang
     * @param sortBy: sắp xếp theo (relevance, price_high, price_low, newest)
     */
    @GET("api/Product/search")
    suspend fun searchProducts(
        @Query("search") search: String,
        @Query("categoryId") categoryId: Int? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 12,
        @Query("sortBy") sortBy: String = "relevance"
    ): PaginatedResponse<Product>
}