package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.ChatMessage
import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.model.PaginatedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    /**
     * Lấy danh sách các cuộc trò chuyện gần đây
     */
    @GET("api/Chat/recent")
    suspend fun getRecentChats(): List<ChatMessage>

    /**
     * Lấy lịch sử tin nhắn với một người dùng
     * @param otherUserId ID của người dùng đối phương
     * @param page Số trang
     * @param pageSize Số lượng mỗi trang
     */
    @GET("api/Chat/history/{otherUserId}")
    suspend fun getChatHistory(
        @Path("otherUserId") otherUserId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): PaginatedResponse<ChatMessage>

    /**
     * Lấy thông tin người dùng theo userName
     * @param userName Tên đăng nhập của người dùng
     */
    @GET("api/User/{userName}")
    suspend fun getUserByUserName(
        @Path("userName") userName: String
    ): ChatUserInfo
}

