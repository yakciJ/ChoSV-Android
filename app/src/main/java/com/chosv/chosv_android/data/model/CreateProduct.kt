package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * DTO để tạo sản phẩm mới
 */
@Serializable
data class CreateProductRequest(
    val productName: String,
    val productDescription: String,
    val price: Long,
    val categoryIds: List<Int>,
    val imageUrls: List<String>
)

/**
 * Response khi tạo sản phẩm thành công
 */
@Serializable
data class CreateProductResponse(
    val message: String
)

