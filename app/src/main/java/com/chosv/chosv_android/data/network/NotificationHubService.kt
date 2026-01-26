package com.chosv.chosv_android.data.network

import android.util.Log
import com.chosv.chosv_android.data.EnvVariable
import com.chosv.chosv_android.data.model.NotificationItem
import com.chosv.chosv_android.preferences.TokenPreferences
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Service quản lý kết nối SignalR cho notification realtime
 */
class NotificationHubService(
    private val tokenPreferences: TokenPreferences
) {
    companion object {
        private const val TAG = "NotificationHubService"
        private val HUB_URL = EnvVariable.BASE_URL.replace("http://", "ws://")
            .replace("https://", "wss://") + "notificationhub"
    }

    private var hubConnection: HubConnection? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Flow để emit thông báo mới nhận được
    private val _incomingNotifications = MutableSharedFlow<NotificationItem>()
    val incomingNotifications: SharedFlow<NotificationItem> = _incomingNotifications.asSharedFlow()

    // Trạng thái kết nối
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    /**
     * Kết nối đến SignalR Hub
     */
    suspend fun connect() {
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected")
            return
        }

        val accessToken = tokenPreferences.accessTokenFlow.firstOrNull()
        if (accessToken.isNullOrBlank()) {
            Log.e(TAG, "No access token available")
            return
        }

        try {
            hubConnection = HubConnectionBuilder.create(HUB_URL)
                .withAccessTokenProvider(io.reactivex.rxjava3.core.Single.defer {
                    io.reactivex.rxjava3.core.Single.just(accessToken)
                })
                .build()

            setupNotificationHandler()

            hubConnection?.start()?.blockingAwait()
            _connectionState.value = true
            Log.d(TAG, "Connected to Notification SignalR hub")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Notification SignalR hub", e)
            _connectionState.value = false
        }
    }

    /**
     * Thiết lập handler cho thông báo
     */
    private fun setupNotificationHandler() {
        // Handler khi nhận được thông báo mới
        hubConnection?.on("ReceiveNotification", { notificationData ->
            scope.launch {
                try {
                    val notification = parseNotificationFromMap(notificationData)
                    if (notification != null) {
                        _incomingNotifications.emit(notification)
                        Log.d(TAG, "Received notification: ${notification.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing received notification", e)
                }
            }
        }, Any::class.java)

        // Handler khi kết nối bị đóng
        hubConnection?.onClosed {
            _connectionState.value = false
            Log.d(TAG, "Notification connection closed: ${it?.message}")
        }
    }

    /**
     * Parse notification từ Map (do SignalR trả về dạng Map)
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseNotificationFromMap(data: Any?): NotificationItem? {
        return try {
            when (data) {
                is Map<*, *> -> {
                    val map = data as Map<String, Any?>
                    NotificationItem(
                        notificationId = (map["notificationId"] as? Number)?.toInt() ?: 0,
                        userId = map["userId"] as? String ?: "",
                        message = map["message"] as? String ?: "",
                        isRead = map["isRead"] as? Boolean ?: false,
                        productId = (map["productId"] as? Number)?.toInt(),
                        userWallPostId = (map["userWallPostId"] as? Number)?.toInt(),
                        fromUserId = map["fromUserId"] as? String,
                        createdAt = map["createdAt"] as? String ?: ""
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing notification from map", e)
            null
        }
    }

    /**
     * Ngắt kết nối
     */
    fun disconnect() {
        try {
            hubConnection?.stop()?.blockingAwait()
            _connectionState.value = false
            Log.d(TAG, "Disconnected from Notification SignalR hub")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from Notification SignalR hub", e)
        }
    }
}
