package com.chosv.chosv_android.ui.screen.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val productDetail: ProductDetail? = null,
    val popularProducts: List<Product> = emptyList(),
    val newestProducts: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val productId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProductPageData()
    }

    private fun loadProductPageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Chạy song song 3 yêu cầu mạng
                val detail = productRepository.getProductDetail(productId)
                val popularResponse = productRepository.getPopularProducts(page = 1, pageSize = 10, daysBack = 30)
                val newestResponse = productRepository.getNewestProducts(page = 1, pageSize = 10)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        productDetail = detail,
                        popularProducts = popularResponse.items,
                        newestProducts = newestResponse.items
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
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
                // Sau khi API thành công, cập nhật lại trạng thái isFavorited của sản phẩm
                updateProductFavoriteState(productId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi thao tác: ${e.message}") }
            }
        }
    }

    // Hàm helper để cập nhật trạng thái trong danh sách sản phẩm
    private fun updateProductFavoriteState(productId: Int) {
        _uiState.update { currentState ->
            val updatedPopular = currentState.popularProducts.map { product ->
                if (product.productId == productId) {
                    product.copy(isFavorited = !product.isFavorited)
                } else {
                    product
                }
            }
            val updatedNewest = currentState.newestProducts.map { product ->
                if (product.productId == productId) {
                    product.copy(isFavorited = !product.isFavorited)
                } else {
                    product
                }
            }
            currentState.copy(popularProducts = updatedPopular, newestProducts = updatedNewest)
        }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            favoriteRepository: FavoriteRepository,
            productId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(productRepository, favoriteRepository, productId) as T
            }
        }
    }
}
