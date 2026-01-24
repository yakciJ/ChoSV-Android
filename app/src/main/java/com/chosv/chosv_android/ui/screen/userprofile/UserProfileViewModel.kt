package com.chosv.chosv_android.ui.screen.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.ChatUserInfo
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ReportEntityType
import com.chosv.chosv_android.data.model.UserWallPost
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import com.chosv.chosv_android.data.repository.ReportRepository
import com.chosv.chosv_android.data.repository.UserProfileRepository
import com.chosv.chosv_android.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val userName: String = "",
    val userInfo: ChatUserInfo? = null,
    val products: List<Product> = emptyList(),
    val wallPosts: List<UserWallPost> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingProducts: Boolean = false,
    val isLoadingWallPosts: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val currentUserName: String? = null,

    // Wall post form
    val newCommentText: String = "",
    val editingWallPostId: Int? = null,
    val editingCommentText: String = "",
    val isSubmittingComment: Boolean = false,

    // Pagination for wall posts
    val wallPostsPage: Int = 1,
    val wallPostsTotalPages: Int = 1,
    val hasMoreWallPosts: Boolean = false,

    // Product count
    val totalProductCount: Int = 0,

    // Report state
    val showReportUserDialog: Boolean = false,
    val showReportCommentDialog: Boolean = false,
    val reportingCommentId: Int? = null,
    val reportingCommentPosterName: String = "",
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false
)

class UserProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val userName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState(userName = userName))
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadUserProfile()
        loadUserProducts()
        loadWallPosts()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                _uiState.update {
                    it.copy(
                        currentUserId = currentUser?.userId,
                        currentUserName = currentUser?.userName
                    )
                }
            } catch (e: Exception) {
                // Ignore, user may not be logged in
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userInfo = userProfileRepository.getUserByUserName(userName)
                _uiState.update { it.copy(userInfo = userInfo, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể tải thông tin người dùng: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadUserProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true) }
            try {
                val response = productRepository.getProductsByUserName(userName, 1, 6)
                _uiState.update {
                    it.copy(
                        products = response.items,
                        totalProductCount = response.totalCount,
                        isLoadingProducts = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingProducts = false) }
            }
        }
    }

    fun loadWallPosts(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWallPosts = true) }
            try {
                val response = userProfileRepository.getUserWallPosts(userName, page, 10)
                _uiState.update { state ->
                    val newPosts = if (page == 1) {
                        response.items
                    } else {
                        state.wallPosts + response.items
                    }
                    state.copy(
                        wallPosts = newPosts,
                        wallPostsPage = response.page,
                        wallPostsTotalPages = response.totalPages,
                        hasMoreWallPosts = response.hasNext,
                        isLoadingWallPosts = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingWallPosts = false) }
            }
        }
    }

    fun loadMoreWallPosts() {
        val currentState = _uiState.value
        if (currentState.hasMoreWallPosts && !currentState.isLoadingWallPosts) {
            loadWallPosts(currentState.wallPostsPage + 1)
        }
    }

    fun toggleFavorite(productId: Int, isCurrentlyFavorited: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyFavorited) {
                    favoriteRepository.deleteFavorite(productId.toString())
                } else {
                    favoriteRepository.addFavorite(productId.toString())
                }
                // Cập nhật trạng thái isFavorited của sản phẩm
                _uiState.update { currentState ->
                    val updatedProducts = currentState.products.map { product ->
                        if (product.productId == productId) {
                            product.copy(isFavorited = !product.isFavorited)
                        } else {
                            product
                        }
                    }
                    currentState.copy(products = updatedProducts)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi thao tác: ${e.message}") }
            }
        }
    }

    // Wall post actions
    fun updateNewCommentText(text: String) {
        _uiState.update { it.copy(newCommentText = text) }
    }

    fun submitNewComment() {
        val userInfo = _uiState.value.userInfo ?: return
        val content = _uiState.value.newCommentText.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingComment = true) }
            try {
                userProfileRepository.createUserWallPost(userInfo.userId, content)
                _uiState.update { it.copy(newCommentText = "", isSubmittingComment = false) }
                loadWallPosts(1) // Reload wall posts
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể đăng bình luận: ${e.message}",
                        isSubmittingComment = false
                    )
                }
            }
        }
    }

    fun startEditingWallPost(wallPost: UserWallPost) {
        _uiState.update {
            it.copy(
                editingWallPostId = wallPost.userWallPostId,
                editingCommentText = wallPost.commentContent
            )
        }
    }

    fun cancelEditingWallPost() {
        _uiState.update {
            it.copy(
                editingWallPostId = null,
                editingCommentText = ""
            )
        }
    }

    fun updateEditingCommentText(text: String) {
        _uiState.update { it.copy(editingCommentText = text) }
    }

    fun submitEditedComment() {
        val wallPostId = _uiState.value.editingWallPostId ?: return
        val content = _uiState.value.editingCommentText.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingComment = true) }
            try {
                userProfileRepository.updateUserWallPost(wallPostId, content)
                _uiState.update {
                    it.copy(
                        editingWallPostId = null,
                        editingCommentText = "",
                        isSubmittingComment = false
                    )
                }
                loadWallPosts(1) // Reload wall posts
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể cập nhật bình luận: ${e.message}",
                        isSubmittingComment = false
                    )
                }
            }
        }
    }

    fun deleteWallPost(wallPostId: Int) {
        viewModelScope.launch {
            try {
                userProfileRepository.deleteUserWallPost(wallPostId)
                loadWallPosts(1) // Reload wall posts
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể xóa bình luận: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Report User functions
    fun showReportUserDialog() {
        _uiState.update { it.copy(showReportUserDialog = true) }
    }

    fun hideReportUserDialog() {
        _uiState.update { it.copy(showReportUserDialog = false) }
    }

    fun reportUser(reason: String) {
        val userInfo = _uiState.value.userInfo ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true) }
            try {
                reportRepository.sendReport(
                    entityId = userInfo.userId,
                    entityType = ReportEntityType.User,
                    reason = reason
                )
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        showReportUserDialog = false,
                        reportSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        error = "Không thể gửi báo cáo: ${e.message}"
                    )
                }
            }
        }
    }

    // Report Comment functions
    fun showReportCommentDialog(commentId: Int, posterName: String) {
        _uiState.update {
            it.copy(
                showReportCommentDialog = true,
                reportingCommentId = commentId,
                reportingCommentPosterName = posterName
            )
        }
    }

    fun hideReportCommentDialog() {
        _uiState.update {
            it.copy(
                showReportCommentDialog = false,
                reportingCommentId = null,
                reportingCommentPosterName = ""
            )
        }
    }

    fun reportComment(reason: String) {
        val commentId = _uiState.value.reportingCommentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true) }
            try {
                reportRepository.sendReport(
                    entityId = commentId.toString(),
                    entityType = ReportEntityType.Comment,
                    reason = reason
                )
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        showReportCommentDialog = false,
                        reportingCommentId = null,
                        reportingCommentPosterName = "",
                        reportSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        error = "Không thể gửi báo cáo: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearReportSuccess() {
        _uiState.update { it.copy(reportSuccess = false) }
    }

    companion object {
        fun provideFactory(
            userProfileRepository: UserProfileRepository,
            productRepository: ProductRepository,
            favoriteRepository: FavoriteRepository,
            userRepository: UserRepository,
            reportRepository: ReportRepository,
            userName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserProfileViewModel(
                    userProfileRepository,
                    productRepository,
                    favoriteRepository,
                    userRepository,
                    reportRepository,
                    userName
                ) as T
            }
        }
    }
}

