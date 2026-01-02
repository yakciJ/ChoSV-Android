package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.ChatMessage
import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.network.ChatApiService

interface ChatRepository {
    /**
     * Lấy danh sách các cuộc trò chuyện gần đây
     */
    suspend fun getRecentChats(): List<ChatMessage>

    /**
     * Lấy lịch sử tin nhắn với một người dùng
     */
    suspend fun getChatHistory(
        otherUserId: String,
        page: Int = 1,
        pageSize: Int = 50
    ): PaginatedResponse<ChatMessage>

    /**
     * Lấy thông tin người dùng theo userName
     */
    suspend fun getUserByUserName(userName: String): ChatUserInfo
}

class ChatRepositoryImpl(
    private val chatApiService: ChatApiService
) : ChatRepository {

    override suspend fun getRecentChats(): List<ChatMessage> {
        return chatApiService.getRecentChats()
    }

    override suspend fun getChatHistory(
        otherUserId: String,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<ChatMessage> {
        return chatApiService.getChatHistory(otherUserId, page, pageSize)
    }

    override suspend fun getUserByUserName(userName: String): ChatUserInfo {
        return chatApiService.getUserByUserName(userName)
    }
}

