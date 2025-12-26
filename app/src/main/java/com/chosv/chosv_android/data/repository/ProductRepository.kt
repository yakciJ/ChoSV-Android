package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail // Thêm import này
import com.chosv.chosv_android.data.network.ProductApiService

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Product.
 * Giúp cho việc testing và thay đổi implementation dễ dàng hơn.
 */
interface ProductRepository {
    suspend fun getNewestProducts(page: Int, pageSize: Int): PaginatedResponse<Product>
    suspend fun getPopularProducts(page: Int, pageSize: Int, daysBack: Int): PaginatedResponse<Product>
    suspend fun getProductDetail(productId: Int): ProductDetail // Thêm hàm này
}

/**
 * Implementation mặc định của ProductRepository.
 * @param productApiService Service của Retrofit để thực hiện các cuộc gọi mạng.
 */
class ProductRepositoryImpl(
    private val productApiService: ProductApiService
) : ProductRepository {

    override suspend fun getNewestProducts(page: Int, pageSize: Int): PaginatedResponse<Product> {
        return productApiService.getNewestProducts(page, pageSize)
    }

    override suspend fun getPopularProducts(page: Int, pageSize: Int, daysBack: Int): PaginatedResponse<Product> {
        return productApiService.getPopularProducts(page, pageSize, daysBack)
    }

    // Thêm implementation cho hàm mới
    override suspend fun getProductDetail(productId: Int): ProductDetail {
        return productApiService.getProductDetail(productId)
    }
}