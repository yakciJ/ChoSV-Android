package com.chosv.chosv_android.data.model

import kotlinx.serialization.Serializable

/**
 * DTO cho một tin nhắn chat
 */
@Serializable
data class ChatMessage(
    val messageId: Int,
    val senderId: String,
    val senderUserName: String,
    val receiverId: String,
    val content: String,
    val createdDate: String,
    val isRead: Boolean,
    val otherUserName: String? = null,
    val otherUserFullName: String? = null,
    val otherUserAvatar: String? = null,
    val otherUserId: String? = null
)

/**
 * DTO cho request gửi tin nhắn qua SignalR
 */
@Serializable
data class SendMessageRequest(
    val ReceiverId: String,
    val Content: String
)

/**
 * DTO cho thông tin người dùng trong chat
 */
@Serializable
data class ChatUserInfo(
    val userId: String,
    val userName: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
    val avatarImage: String? = null,
    val bio: String? = null,
    val address: String? = null,
    val createdAt: String? = null
)

