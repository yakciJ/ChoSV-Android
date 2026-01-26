package com.chosv.chosv_android.data.repository

import com.chosv.chosv_android.data.model.NotificationItem
import com.chosv.chosv_android.data.model.NotificationListResponse
import com.chosv.chosv_android.data.network.NotificationApiService

/**
 * Interface cho repository quản lý các hoạt động liên quan đến Notification
 */
interface NotificationRepository {
    /**
     * Lấy danh sách thông báo
     */
    suspend fun getNotifications(page: Int = 1, pageSize: Int = 20): NotificationListResponse

    /**
     * Lấy số lượng thông báo chưa đọc
     */
    suspend fun getUnreadCount(): Int

    /**
     * Đánh dấu thông báo đã đọc
     */
    suspend fun markAsRead(notificationId: Int)

    /**
     * Xóa thông báo
     */
    suspend fun deleteNotification(notificationId: Int)

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    suspend fun markAllAsRead(notifications: List<NotificationItem>)
}

/**
 * Implementation của NotificationRepository
 */
class NotificationRepositoryImpl(
    private val notificationApiService: NotificationApiService
) : NotificationRepository {

    override suspend fun getNotifications(page: Int, pageSize: Int): NotificationListResponse {
        return notificationApiService.getNotifications(page, pageSize)
    }

    override suspend fun getUnreadCount(): Int {
        return notificationApiService.getUnreadCount()
    }

    override suspend fun markAsRead(notificationId: Int) {
        notificationApiService.markAsRead(notificationId)
    }

    override suspend fun deleteNotification(notificationId: Int) {
        notificationApiService.deleteNotification(notificationId)
    }

    override suspend fun markAllAsRead(notifications: List<NotificationItem>) {
        notifications.filter { !it.isRead }.forEach { notification ->
            try {
                notificationApiService.markAsRead(notification.notificationId)
            } catch (_: Exception) {
                // Ignore individual errors
            }
        }
    }
}
