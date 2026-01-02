package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.model.CreateUserWallPostRequest
import com.chosv.chosv_android.data.model.MessageResponse
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.UpdateUserWallPostRequest
import com.chosv.chosv_android.data.model.UserWallPost
import com.chosv.chosv_android.data.network.UserApiService

interface UserProfileRepository {
    /**
     * Lấy thông tin người dùng theo userName
     */
    suspend fun getUserByUserName(userName: String): ChatUserInfo

    /**
     * Lấy danh sách bình luận trên tường của người dùng
     */
    suspend fun getUserWallPosts(
        userName: String,
        page: Int = 1,
        pageSize: Int = 10
    ): PaginatedResponse<UserWallPost>

    /**
     * Tạo bình luận mới trên tường người dùng
     */
    suspend fun createUserWallPost(
        userWallOwnerId: String,
        commentContent: String
    ): MessageResponse

    /**
     * Cập nhật bình luận trên tường người dùng
     */
    suspend fun updateUserWallPost(
        userWallPostId: Int,
        commentContent: String
    ): MessageResponse

    /**
     * Xóa bình luận trên tường người dùng
     */
    suspend fun deleteUserWallPost(userWallPostId: Int): String
}

class UserProfileRepositoryImpl(
    private val userApiService: UserApiService
) : UserProfileRepository {

    override suspend fun getUserByUserName(userName: String): ChatUserInfo {
        return userApiService.getUserByUserName(userName)
    }

    override suspend fun getUserWallPosts(
        userName: String,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<UserWallPost> {
        return userApiService.getUserWallPosts(userName, page, pageSize)
    }

    override suspend fun createUserWallPost(
        userWallOwnerId: String,
        commentContent: String
    ): MessageResponse {
        return userApiService.createUserWallPost(
            CreateUserWallPostRequest(userWallOwnerId, commentContent)
        )
    }

    override suspend fun updateUserWallPost(
        userWallPostId: Int,
        commentContent: String
    ): MessageResponse {
        return userApiService.updateUserWallPost(
            UpdateUserWallPostRequest(userWallPostId, commentContent)
        )
    }

    override suspend fun deleteUserWallPost(userWallPostId: Int): String {
        return userApiService.deleteUserWallPost(userWallPostId)
    }
}

