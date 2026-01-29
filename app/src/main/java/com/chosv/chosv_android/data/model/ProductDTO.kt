package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * DTO cho sản phẩm của người dùng hiện tại (từ /api/Product/me)
 */
@Serializable
data class MyProduct(
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val productDescription: String,
    val status: String,
    val sellerId: String,
    val sellerName: String,
    val createdDate: String,
    val categories: List<Category>,
    val firstImageUrl: String
)

/**
 * DTO cho request cập nhật sản phẩm
 */
@Serializable
data class UpdateProductRequest(
    val productName: String,
    val productDescription: String,
    val price: Long,
    val categoryIds: List<Int>,
    val imageUrls: List<String>
)

/**
 * Response chung cho các thao tác (update, delete, ...)
 */
@Serializable
data class MessageResponse(
    val message: String
)

/**
 * DTO cho một Category
 */
@Serializable
data class Category(
    val categoryId: Int,
    val imageUrl: String,
    val categoryName: String
)

/**
 * DTO cho chi tiết một sản phẩm
 */
@Serializable
data class ProductDetail(
    val productId: Int,
    val productName: String,
    val sellerId: String,
    val sellerName: String,
    val sellerAddress: String,
    val sellerPhone: String,
    val productDescription: String,
    val price: Double, // Sửa từ Long thành Double
    val status: String,
    val createdDate: String,
    val sellerFullName: String,
    val sellerAvatarImage: String? = null, // Có thể null
    val sellerEmail: String,
    val sellerUniversity: String? = null,
    val sellerJoinedDate: String,
    val productImages: List<String>,
    val favoriteCount: Int,
    val isFavorite: Boolean,
    val parentCategoryId: Int,
    val parentCategoryName: String,
    val childCategoryId: Int,
    val childCategoryName: String
)

/**
 * DTO cho một Product
 */
@Serializable
data class Product(
    val productId: Int,
    val productName: String,
    val price: Double,
    val status: String,
    val firstImageUrl: String,
    val createdDate: String,
    val sellerName: String,
    val sellerFullName: String,
    val sellerAvatar: String,
    val isFavorited: Boolean,
    val favoriteCount: Int,
    val categories: List<Category>
)

/**
 * DTO chung cho các response có phân trang
 * <T> là kiểu dữ liệu của các item trong danh sách, ở đây sẽ là Product
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)