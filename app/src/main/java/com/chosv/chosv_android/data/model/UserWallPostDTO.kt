package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * DTO cho một bình luận trên tường của người dùng
 */
@Serializable
data class UserWallPost(
    val userWallPostId: Int,
    val userWallOwnerId: String,
    val posterId: String,
    val posterAvatarImage: String? = null,
    val posterUserName: String,
    val posterFullName: String,
    val commentContent: String,
    val createdAt: String
)

/**
 * Request để tạo bình luận mới trên tường người dùng
 */
@Serializable
data class CreateUserWallPostRequest(
    val userWallOwnerId: String,
    val commentContent: String
)

/**
 * Request để cập nhật bình luận trên tường người dùng
 */
@Serializable
data class UpdateUserWallPostRequest(
    val userWallPostId: Int,
    val commentContent: String
)

