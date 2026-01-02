package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.model.CreateUserWallPostRequest
import com.chosv.chosv_android.data.model.MessageResponse
import com.chosv.chosv_android.data.model.PaginatedResponse
import com.chosv.chosv_android.data.model.UpdateUserWallPostRequest
import com.chosv.chosv_android.data.model.UserWallPost
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    /**
     * Lấy thông tin người dùng theo userName
     * @param userName Tên đăng nhập của người dùng
     */
    @GET("api/User/{userName}")
    suspend fun getUserByUserName(
        @Path("userName") userName: String
    ): ChatUserInfo

    /**
     * Lấy danh sách bình luận trên tường của người dùng
     * @param userName Tên đăng nhập của chủ tường
     * @param page Số trang
     * @param pageSize Số lượng mỗi trang
     */
    @GET("api/UserWallPost/{userName}")
    suspend fun getUserWallPosts(
        @Path("userName") userName: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): PaginatedResponse<UserWallPost>

    /**
     * Tạo bình luận mới trên tường người dùng
     */
    @POST("api/UserWallPost")
    suspend fun createUserWallPost(
        @Body request: CreateUserWallPostRequest
    ): MessageResponse

    /**
     * Cập nhật bình luận trên tường người dùng
     */
    @PUT("api/UserWallPost")
    suspend fun updateUserWallPost(
        @Body request: UpdateUserWallPostRequest
    ): MessageResponse

    /**
     * Xóa bình luận trên tường người dùng
     * @param userWallPostId ID của bình luận cần xóa
     */
    @DELETE("api/UserWallPost/{userWallPostId}")
    suspend fun deleteUserWallPost(
        @Path("userWallPostId") userWallPostId: Int
    ): String
}

