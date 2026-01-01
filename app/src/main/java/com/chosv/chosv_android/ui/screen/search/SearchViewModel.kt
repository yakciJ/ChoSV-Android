package com.chosv.chosv_android.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.repository.CategoryRepository
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Các tùy chọn sắp xếp kết quả tìm kiếm
 */
enum class SortOption(val value: String, val displayName: String) {
    RELEVANCE("relevance", "Liên quan nhất"),
    PRICE_LOW("price_low", "Giá thấp đến cao"),
    PRICE_HIGH("price_high", "Giá cao đến thấp"),
    NEWEST("newest", "Mới nhất")
}

data class SearchUiState(
    val searchQuery: String = "",
    val products: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,

    // Filter options
    val selectedCategoryId: Int? = null,
    val selectedCategoryName: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,

    // Categories for filter
    val categories: List<CategoryTree> = emptyList(),
    val showFilterSheet: Boolean = false
)

class SearchViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val pageSize = 12
    private var searchJob: Job? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategoryTree()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                // Ignore category loading error
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Debounce search - đợi 500ms sau khi người dùng ngừng gõ
        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(500)
                searchProducts(1)
            }
        } else {
            // Clear results nếu query rỗng
            _uiState.update {
                it.copy(
                    products = emptyList(),
                    totalCount = 0,
                    currentPage = 1,
                    totalPages = 1,
                    hasPrevious = false,
                    hasNext = false,
                    hasSearched = false
                )
            }
        }
    }

    fun performSearch() {
        if (_uiState.value.searchQuery.isNotBlank()) {
            searchProducts(1)
        }
    }

    private fun searchProducts(page: Int) {
        val currentState = _uiState.value
        if (currentState.searchQuery.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = productRepository.searchProducts(
                    search = currentState.searchQuery,
                    categoryId = currentState.selectedCategoryId,
                    minPrice = currentState.minPrice,
                    maxPrice = currentState.maxPrice,
                    page = page,
                    pageSize = pageSize,
                    sortBy = currentState.sortBy.value
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSearched = true,
                        products = response.items,
                        totalCount = response.totalCount,
                        currentPage = response.page,
                        totalPages = response.totalPages,
                        hasPrevious = response.hasPrevious,
                        hasNext = response.hasNext
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSearched = true,
                        error = e.message
                    )
                }
            }
        }
    }

    fun goToNextPage() {
        if (_uiState.value.hasNext) {
            searchProducts(_uiState.value.currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (_uiState.value.hasPrevious) {
            searchProducts(_uiState.value.currentPage - 1)
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

    fun showFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }

    fun hideFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun updateCategory(categoryId: Int?, categoryName: String?) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                selectedCategoryName = categoryName
            )
        }
    }

    fun updateMinPrice(price: Double?) {
        _uiState.update { it.copy(minPrice = price) }
    }

    fun updateMaxPrice(price: Double?) {
        _uiState.update { it.copy(maxPrice = price) }
    }

    fun updateSortBy(sortOption: SortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
        // Tự động search lại khi thay đổi sort
        if (_uiState.value.searchQuery.isNotBlank()) {
            searchProducts(1)
        }
    }

    fun applyFilters() {
        hideFilterSheet()
        if (_uiState.value.searchQuery.isNotBlank()) {
            searchProducts(1)
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCategoryId = null,
                selectedCategoryName = null,
                minPrice = null,
                maxPrice = null,
                sortBy = SortOption.RELEVANCE
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            favoriteRepository: FavoriteRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(
                    productRepository,
                    categoryRepository,
                    favoriteRepository
                ) as T
            }
        }
    }
}

