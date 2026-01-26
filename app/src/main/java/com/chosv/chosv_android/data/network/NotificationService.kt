package com.chosv.chosv_android.data.network

import com.chosv.chosv_android.data.model.NotificationActionResponse
import com.chosv.chosv_android.data.model.NotificationListResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    /**
     * Lấy danh sách thông báo
     * @param page Số trang (mặc định 1)
     * @param pageSize Số lượng mỗi trang (mặc định 20)
     */
    @GET("api/Notification")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): NotificationListResponse

    /**
     * Lấy số lượng thông báo chưa đọc
     */
    @GET("api/Notification/unreadCount")
    suspend fun getUnreadCount(): Int

    /**
     * Đánh dấu thông báo đã đọc
     * @param notificationId ID của thông báo
     */
    @PUT("api/Notification/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: Int
    ): NotificationActionResponse

    /**
     * Xóa thông báo
     * @param notificationId ID của thông báo
     */
    @DELETE("api/Notification/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: Int
    ): NotificationActionResponse
}
