package com.chosv.chosv_android.ui.screen.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.ChatMessage
import com.chosv.chosv_android.data.network.ChatHubService
import com.chosv.chosv_android.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagesUiState(
    val recentChats: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = false
)

class MessagesViewModel(
    private val chatRepository: ChatRepository,
    private val chatHubService: ChatHubService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadRecentChats()
        connectToHub()
        observeIncomingMessages()
    }

    private fun connectToHub() {
        viewModelScope.launch {
            try {
                chatHubService.connect()
            } catch (e: Exception) {
                // Ignore connection error, will retry when sending
            }
        }

        viewModelScope.launch {
            chatHubService.connectionState.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            chatHubService.incomingMessages.collect { message ->
                // Khi có tin nhắn mới, cập nhật danh sách recent chats
                updateRecentChatWithNewMessage(message)
            }
        }
    }

    private fun updateRecentChatWithNewMessage(newMessage: ChatMessage) {
        _uiState.update { state ->
            val currentChats = state.recentChats.toMutableList()
            // Tìm và cập nhật hoặc thêm mới
            val existingIndex = currentChats.indexOfFirst {
                it.otherUserId == newMessage.senderId ||
                it.senderId == newMessage.senderId
            }

            if (existingIndex >= 0) {
                // Cập nhật tin nhắn mới nhất và đưa lên đầu
                currentChats.removeAt(existingIndex)
            }

            // Thêm tin nhắn mới lên đầu danh sách
            val updatedMessage = newMessage.copy(
                otherUserId = newMessage.senderId,
                otherUserName = newMessage.senderUserName
            )
            currentChats.add(0, updatedMessage)

            state.copy(recentChats = currentChats)
        }
    }

    fun loadRecentChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val chats = chatRepository.getRecentChats()
                _uiState.update {
                    it.copy(
                        recentChats = chats,
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            chatRepository: ChatRepository,
            chatHubService: ChatHubService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MessagesViewModel(chatRepository, chatHubService) as T
            }
        }
    }
}

