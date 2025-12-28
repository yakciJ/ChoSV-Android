package com.chosv.chosv_android.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.repository.ProductRepository
import com.chosv.chosv_android.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lớp này định nghĩa trạng thái của giao diện màn hình Home
data class HomeUiState(
    val popularProducts: List<Product> = emptyList(),
    val newestProducts: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Gọi hàm tải dữ liệu ngay khi ViewModel được tạo
        loadHomePageData()
    }

    private fun loadHomePageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Gọi đồng thời 2 API để lấy 6 sản phẩm cho mỗi loại
                val popularResponse = productRepository.getPopularProducts(page = 1, pageSize = 6, daysBack = 30)
                val newestResponse = productRepository.getNewestProducts(page = 1, pageSize = 6)

                // Cập nhật lại State với dữ liệu mới
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        popularProducts = popularResponse.items,
                        newestProducts = newestResponse.items
                    )
                }
            } catch (e: Exception) {
                // Nếu có lỗi, cập nhật state lỗi
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
                // TODO: Xử lý lỗi khi gọi API (ví dụ: hiển thị Snackbar)
                _uiState.update { it.copy(error = "Lỗi thao tác: ${e.message}") }
            }
        }
    }

    // Hàm helper để cập nhật trạng thái trong danh sách sản phẩm
    private fun updateProductFavoriteState(productId: Int) {
        _uiState.update { currentState ->
            val updatedPopular = currentState.popularProducts.map { product ->
                if (product.productId == productId) {
                    product.copy(isFavorited = !product.isFavorited) // Đảo ngược trạng thái
                } else {
                    product
                }
            }
            val updatedNewest = currentState.newestProducts.map { product ->
                if (product.productId == productId) {
                    product.copy(isFavorited = !product.isFavorited) // Đảo ngược trạng thái
                } else {
                    product
                }
            }
            currentState.copy(popularProducts = updatedPopular, newestProducts = updatedNewest)
        }
    }


    // Factory là một pattern chuẩn để bạn có thể "truyền" ProductRepository
    // từ bên ngoài vào trong ViewModel khi khởi tạo nó.
    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            favoriteRepository: FavoriteRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(productRepository, favoriteRepository) as T
            }
        }
    }
}