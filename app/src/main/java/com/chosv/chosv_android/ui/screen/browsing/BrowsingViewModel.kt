package com.chosv.chosv_android.ui.screen.browsing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.repository.CategoryRepository
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loại danh sách sản phẩm đang duyệt
 */
sealed class BrowsingType {
    data object Newest : BrowsingType()
    data object Popular : BrowsingType()
    data class Category(val categoryId: Int, val categoryName: String) : BrowsingType()
    data class User(val userName: String) : BrowsingType()
    data class Similar(val productId: Int, val productName: String) : BrowsingType()
}

data class BrowsingUiState(
    val browsingType: BrowsingType = BrowsingType.Newest,
    val title: String = "",
    val products: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class BrowsingViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val browsingType: BrowsingType
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowsingUiState(browsingType = browsingType))
    val uiState: StateFlow<BrowsingUiState> = _uiState.asStateFlow()

    private val pageSize = 12

    init {
        // Set title based on browsing type
        val title = when (browsingType) {
            is BrowsingType.Newest -> "Sản phẩm mới nhất"
            is BrowsingType.Popular -> "Sản phẩm nổi bật"
            is BrowsingType.Category -> browsingType.categoryName
            is BrowsingType.User -> "Sản phẩm của \"${browsingType.userName}\""
            is BrowsingType.Similar -> "Sản phẩm tương tự \"${browsingType.productName}\""
        }
        _uiState.update { it.copy(title = title) }
        loadProducts(1)
    }

    fun loadProducts(page: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (browsingType) {
                    is BrowsingType.Newest -> {
                        val response = productRepository.getNewestProducts(page, pageSize)
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
                    }
                    is BrowsingType.Popular -> {
                        val response = productRepository.getPopularProducts(page, pageSize, 30)
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
                    }
                    is BrowsingType.Category -> {
                        val response = categoryRepository.getCategoryProducts(
                            browsingType.categoryId, page, pageSize
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = response.name,
                                products = response.products.items,
                                totalCount = response.products.totalCount,
                                currentPage = response.products.page,
                                totalPages = response.products.totalPages,
                                hasPrevious = response.products.hasPrevious,
                                hasNext = response.products.hasNext
                            )
                        }
                    }
                    is BrowsingType.User -> {
                        val response = productRepository.getProductsByUserName(
                            browsingType.userName, page, pageSize
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
                    }
                    is BrowsingType.Similar -> {
                        val response = productRepository.getSimilarProducts(
                            browsingType.productId, page, pageSize
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
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            favoriteRepository: FavoriteRepository,
            browsingType: BrowsingType
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BrowsingViewModel(
                    productRepository,
                    categoryRepository,
                    favoriteRepository,
                    browsingType
                ) as T
            }
        }
    }
}

