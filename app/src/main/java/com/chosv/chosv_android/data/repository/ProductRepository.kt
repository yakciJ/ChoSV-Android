package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.CreateProductRequest
import com.chosv.chosv_android.data.model.CreateProductResponse
import com.chosv.chosv_android.data.model.MessageResponse
import com.chosv.chosv_android.data.model.MyProduct
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.model.UpdateProductRequest
import com.chosv.chosv_android.data.network.ProductApiService

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Product.
 * Giúp cho việc testing và thay đổi implementation dễ dàng hơn.
 */
interface ProductRepository {
    suspend fun getNewestProducts(page: Int, pageSize: Int): PaginatedResponse<Product>
    suspend fun getPopularProducts(page: Int, pageSize: Int, daysBack: Int): PaginatedResponse<Product>
    suspend fun getProductDetail(productId: Int): ProductDetail
    suspend fun createProduct(request: CreateProductRequest): CreateProductResponse

    // Các hàm cho quản lý sản phẩm của tôi
    suspend fun getMyProducts(page: Int, pageSize: Int, status: String?): PaginatedResponse<MyProduct>
    suspend fun updateProduct(productId: Int, request: UpdateProductRequest): MessageResponse
    suspend fun updateProductStatus(productId: Int, status: String): MessageResponse
    suspend fun deleteProduct(productId: Int): MessageResponse

    // Tìm kiếm sản phẩm
    suspend fun searchProducts(
        search: String,
        categoryId: Int?,
        minPrice: Double?,
        maxPrice: Double?,
        page: Int,
        pageSize: Int,
        sortBy: String
    ): PaginatedResponse<Product>

    // Lấy sản phẩm theo userName
    suspend fun getProductsByUserName(
        userName: String,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<Product>

    // Lấy sản phẩm tương tự
    suspend fun getSimilarProducts(
        productId: Int,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<Product>
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

    override suspend fun getProductDetail(productId: Int): ProductDetail {
        return productApiService.getProductDetail(productId)
    }

    override suspend fun createProduct(request: CreateProductRequest): CreateProductResponse {
        return productApiService.createProduct(request)
    }

    override suspend fun getMyProducts(page: Int, pageSize: Int, status: String?): PaginatedResponse<MyProduct> {
        return productApiService.getMyProducts(page, pageSize, status)
    }

    override suspend fun updateProduct(productId: Int, request: UpdateProductRequest): MessageResponse {
        return productApiService.updateProduct(productId, request)
    }

    override suspend fun updateProductStatus(productId: Int, status: String): MessageResponse {
        return productApiService.updateProductStatus(productId, status)
    }

    override suspend fun deleteProduct(productId: Int): MessageResponse {
        return productApiService.deleteProduct(productId)
    }

    override suspend fun searchProducts(
        search: String,
        categoryId: Int?,
        minPrice: Double?,
        maxPrice: Double?,
        page: Int,
        pageSize: Int,
        sortBy: String
    ): PaginatedResponse<Product> {
        return productApiService.searchProducts(
            search = search,
            categoryId = categoryId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            page = page,
            pageSize = pageSize,
            sortBy = sortBy
        )
    }

    override suspend fun getProductsByUserName(
        userName: String,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<Product> {
        return productApiService.getProductsByUserName(userName, page, pageSize)
    }

    override suspend fun getSimilarProducts(
        productId: Int,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<Product> {
        return productApiService.getSimilarProducts(productId, page, pageSize)
    }
}