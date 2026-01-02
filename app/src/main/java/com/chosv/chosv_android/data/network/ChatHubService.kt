package com.chosv.chosv_android.data.network

import android.util.Log
import com.chosv.chosv_android.data.EnvVariable
import com.chosv.chosv_android.data.model.ChatMessage
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
import kotlinx.serialization.json.Json

/**
 * Service quản lý kết nối SignalR cho chat realtime
 */
class ChatHubService(
    private val tokenPreferences: TokenPreferences
) {
    companion object {
        private const val TAG = "ChatHubService"
        private val HUB_URL = EnvVariable.BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "chathub"
    }

    private var hubConnection: HubConnection? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Flow để emit tin nhắn mới nhận được
    private val _incomingMessages = MutableSharedFlow<ChatMessage>()
    val incomingMessages: SharedFlow<ChatMessage> = _incomingMessages.asSharedFlow()

    // Flow để emit tin nhắn đã gửi thành công
    private val _sentMessages = MutableSharedFlow<ChatMessage>()
    val sentMessages: SharedFlow<ChatMessage> = _sentMessages.asSharedFlow()

    // Flow để emit lỗi
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()

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

            setupMessageHandlers()

            hubConnection?.start()?.blockingAwait()
            _connectionState.value = true
            Log.d(TAG, "Connected to SignalR hub")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to SignalR hub", e)
            _connectionState.value = false
        }
    }

    /**
     * Thiết lập các handler cho tin nhắn
     */
    private fun setupMessageHandlers() {
        // Handler khi nhận được tin nhắn mới
        hubConnection?.on("ReceiveMessage", { messageData ->
            scope.launch {
                try {
                    val message = parseMessageFromMap(messageData)
                    if (message != null) {
                        _incomingMessages.emit(message)
                        Log.d(TAG, "Received message: ${message.content}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing received message", e)
                }
            }
        }, Any::class.java)

        // Handler khi tin nhắn đã gửi thành công
        hubConnection?.on("MessageSent", { messageData ->
            scope.launch {
                try {
                    val message = parseMessageFromMap(messageData)
                    if (message != null) {
                        _sentMessages.emit(message)
                        Log.d(TAG, "Message sent successfully: ${message.content}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sent message confirmation", e)
                }
            }
        }, Any::class.java)

        // Handler cho lỗi
        hubConnection?.on("Error", { errorMessage ->
            scope.launch {
                val error = errorMessage.toString()
                _errors.emit(error)
                Log.e(TAG, "SignalR Error: $error")
            }
        }, String::class.java)

        // Handler khi kết nối bị đóng
        hubConnection?.onClosed {
            _connectionState.value = false
            Log.d(TAG, "Connection closed: ${it?.message}")
        }
    }

    /**
     * Parse message từ Map (do SignalR trả về dạng Map)
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseMessageFromMap(data: Any?): ChatMessage? {
        return try {
            when (data) {
                is Map<*, *> -> {
                    val map = data as Map<String, Any?>
                    ChatMessage(
                        messageId = (map["messageId"] as? Number)?.toInt() ?: 0,
                        senderId = map["senderId"] as? String ?: "",
                        senderUserName = map["senderUserName"] as? String ?: "",
                        receiverId = map["receiverId"] as? String ?: "",
                        content = map["content"] as? String ?: "",
                        createdDate = map["createdDate"] as? String ?: "",
                        isRead = map["isRead"] as? Boolean ?: false,
                        otherUserName = map["otherUserName"] as? String,
                        otherUserFullName = map["otherUserFullName"] as? String,
                        otherUserAvatar = map["otherUserAvatar"] as? String,
                        otherUserId = map["otherUserId"] as? String
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message from map", e)
            null
        }
    }

    /**
     * Gửi tin nhắn
     * @param receiverId ID người nhận
     * @param content Nội dung tin nhắn
     */
    fun sendMessage(receiverId: String, content: String) {
        if (hubConnection?.connectionState != HubConnectionState.CONNECTED) {
            scope.launch {
                _errors.emit("Chưa kết nối đến server")
            }
            return
        }

        try {
            val messageData = mapOf(
                "ReceiverId" to receiverId,
                "Content" to content
            )
            hubConnection?.send("SendMessage", messageData)
            Log.d(TAG, "Sending message to $receiverId: $content")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            scope.launch {
                _errors.emit("Không thể gửi tin nhắn: ${e.message}")
            }
        }
    }

    /**
     * Ngắt kết nối
     */
    fun disconnect() {
        try {
            hubConnection?.stop()?.blockingAwait()
            _connectionState.value = false
            Log.d(TAG, "Disconnected from SignalR hub")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from SignalR hub", e)
        }
    }
}

