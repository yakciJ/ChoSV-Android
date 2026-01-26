package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * Data class đại diện cho một thông báo
 */
@Serializable
data class NotificationItem(
    val notificationId: Int,
    val userId: String,
    val message: String,
    val isRead: Boolean,
    val productId: Int? = null,
    val userWallPostId: Int? = null,
    val fromUserId: String? = null,
    val createdAt: String
)

/**
 * Response khi lấy danh sách thông báo
 */
@Serializable
data class NotificationListResponse(
    val items: List<NotificationItem>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)

/**
 * Response khi thao tác với thông báo (đánh dấu đã đọc, xóa)
 */
@Serializable
data class NotificationActionResponse(
    val message: String
)
