package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * Response cho API lấy sản phẩm theo danh mục
 */
@Serializable
data class CategoryProductsResponse(
    val categoryId: Int,
    val name: String,
    val imageUrl: String,
    val description: String,
    val productCount: Int,
    val products: PaginatedResponse<Product>
)

