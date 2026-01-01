package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * Response khi upload 1 ảnh
 */
@Serializable
data class UploadImageResponse(
    val imageUrl: String,
    val message: String
)

/**
 * Response khi upload nhiều ảnh
 */
@Serializable
data class UploadMultipleImagesResponse(
    val imageUrls: List<String>,
    val message: String
)

