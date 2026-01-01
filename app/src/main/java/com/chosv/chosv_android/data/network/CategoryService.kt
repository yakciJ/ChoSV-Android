package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.CategoryProductsResponse
import com.chosv.chosv_android.data.model.CategoryTree
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryApiService {

    /**
     * Lấy danh sách category dạng cây (bao gồm category cha và con)
     * API này không cần xác thực
     */
    @GET("api/Category/tree")
    suspend fun getCategoryTree(): List<CategoryTree>

    /**
     * Lấy danh sách sản phẩm theo danh mục
     * @param categoryId: ID của danh mục
     * @param page: số trang
     * @param pageSize: số lượng sản phẩm trên mỗi trang
     */
    @GET("api/Category/{categoryId}/products")
    suspend fun getCategoryProducts(
        @Path("categoryId") categoryId: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): CategoryProductsResponse
}
