package com.chosv.chosv_android.ui.screen.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.MyProduct
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Các tab trạng thái sản phẩm
 */
enum class ProductStatusTab(val value: String?, val displayName: String) {
    ALL(null, "Tất cả"),
    PENDING("Pending", "Chờ duyệt"),
    APPROVED("Approved", "Đang bán"),
    REJECTED("Rejected", "Bị từ chối"),
    SOLD("Sold", "Đã bán")
}

data class ManagementUiState(
    val products: List<MyProduct> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTab: ProductStatusTab = ProductStatusTab.ALL,

    // Dialog states
    val showDeleteDialog: Boolean = false,
    val productToDelete: MyProduct? = null,
    val showStatusDialog: Boolean = false,
    val productToChangeStatus: MyProduct? = null,

    // Action states
    val isDeleting: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val successMessage: String? = null
)

class ManagementViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagementUiState())
    val uiState: StateFlow<ManagementUiState> = _uiState.asStateFlow()

    private val pageSize = 10

    init {
        loadProducts(1)
    }

    fun loadProducts(page: Int, status: String? = _uiState.value.selectedTab.value) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = productRepository.getMyProducts(
                    page = page,
                    pageSize = pageSize,
                    status = status
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        products = response.items,
                        totalCount = response.totalCount,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        hasPrevious = response.hasPrevious,
                        hasNext = response.hasNext
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectTab(tab: ProductStatusTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.update { it.copy(selectedTab = tab) }
            loadProducts(1, tab.value)
        }
    }

    fun goToNextPage() {
        if (_uiState.value.hasNext) {
            loadProducts(_uiState.value.currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (_uiState.value.hasPrevious) {
            loadProducts(_uiState.value.currentPage - 1)
        }
    }

    // --- Delete Product ---
    fun showDeleteConfirmation(product: MyProduct) {
        _uiState.update {
            it.copy(showDeleteDialog = true, productToDelete = product)
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(showDeleteDialog = false, productToDelete = null)
        }
    }

    fun confirmDelete() {
        val product = _uiState.value.productToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                val response = productRepository.deleteProduct(product.productId)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        showDeleteDialog = false,
                        productToDelete = null,
                        successMessage = response.message
                    )
                }
                // Reload lại danh sách
                loadProducts(_uiState.value.currentPage)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        error = "Xóa thất bại: ${e.message}"
                    )
                }
            }
        }
    }

    // --- Change Status ---
    fun showStatusDialog(product: MyProduct) {
        _uiState.update {
            it.copy(showStatusDialog = true, productToChangeStatus = product)
        }
    }

    fun dismissStatusDialog() {
        _uiState.update {
            it.copy(showStatusDialog = false, productToChangeStatus = null)
        }
    }

    fun changeStatus(newStatus: String) {
        val product = _uiState.value.productToChangeStatus ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true) }
            try {
                val response = productRepository.updateProductStatus(product.productId, newStatus)
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        showStatusDialog = false,
                        productToChangeStatus = null,
                        successMessage = response.message
                    )
                }
                // Reload lại danh sách
                loadProducts(_uiState.value.currentPage)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        error = "Cập nhật trạng thái thất bại: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ManagementViewModel(productRepository) as T
            }
        }
    }
}

