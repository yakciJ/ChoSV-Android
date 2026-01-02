package com.chosv.chosv_android.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.ChatMessage
import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.network.ChatHubService
import com.chosv.chosv_android.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserInfo: ChatUserInfo? = null,
    val messages: List<ChatMessage> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatHubService: ChatHubService,
    private val otherUserId: String,
    private val otherUserName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(
        otherUserId = otherUserId,
        otherUserName = otherUserName
    ))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val pageSize = 50

    init {
        connectToHub()
        loadUserInfo()
        loadChatHistory()
        observeMessages()
    }

    private fun connectToHub() {
        viewModelScope.launch {
            try {
                chatHubService.connect()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể kết nối: ${e.message}") }
            }
        }

        viewModelScope.launch {
            chatHubService.connectionState.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
    }

    private fun observeMessages() {
        // Lắng nghe tin nhắn nhận được
        viewModelScope.launch {
            chatHubService.incomingMessages.collect { message ->
                // Chỉ thêm tin nhắn nếu từ người đang chat
                if (message.senderId == otherUserId) {
                    addNewMessage(message)
                }
            }
        }

        // Lắng nghe tin nhắn đã gửi thành công
        viewModelScope.launch {
            chatHubService.sentMessages.collect { message ->
                if (message.receiverId == otherUserId) {
                    addNewMessage(message)
                    _uiState.update { it.copy(isSending = false, messageInput = "") }
                }
            }
        }

        // Lắng nghe lỗi
        viewModelScope.launch {
            chatHubService.errors.collect { error ->
                _uiState.update { it.copy(error = error, isSending = false) }
            }
        }
    }

    private fun addNewMessage(message: ChatMessage) {
        _uiState.update { state ->
            // Kiểm tra xem tin nhắn đã tồn tại chưa
            if (state.messages.any { it.messageId == message.messageId }) {
                state
            } else {
                state.copy(messages = state.messages + message)
            }
        }
    }

    private fun loadUserInfo() {
        if (otherUserName.isBlank()) return

        viewModelScope.launch {
            try {
                val userInfo = chatRepository.getUserByUserName(otherUserName)
                _uiState.update { it.copy(otherUserInfo = userInfo) }
            } catch (e: Exception) {
                // Ignore error, user info is optional
            }
        }
    }

    fun loadChatHistory(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = chatRepository.getChatHistory(
                    otherUserId = otherUserId,
                    page = page,
                    pageSize = pageSize
                )

                _uiState.update { state ->
                    // API trả về tin nhắn từ cũ đến mới, không cần đảo ngược
                    val newMessages = if (page == 1) {
                        response.items
                    } else {
                        // Load thêm tin cũ hơn (page tiếp theo), thêm vào cuối list
                        state.messages + response.items
                    }

                    state.copy(
                        messages = newMessages,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        hasMore = response.hasNext,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể tải tin nhắn: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadMoreMessages() {
        val currentState = _uiState.value
        if (currentState.hasMore && !currentState.isLoading) {
            loadChatHistory(currentState.currentPage + 1)
        }
    }

    fun updateMessageInput(input: String) {
        _uiState.update { it.copy(messageInput = input) }
    }

    fun sendMessage() {
        val content = _uiState.value.messageInput.trim()
        if (content.isBlank()) return

        _uiState.update { it.copy(isSending = true) }
        chatHubService.sendMessage(otherUserId, content)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            chatRepository: ChatRepository,
            chatHubService: ChatHubService,
            otherUserId: String,
            otherUserName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(
                    chatRepository,
                    chatHubService,
                    otherUserId,
                    otherUserName
                ) as T
            }
        }
    }
}

