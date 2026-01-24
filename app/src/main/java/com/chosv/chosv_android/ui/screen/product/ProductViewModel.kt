package com.chosv.chosv_android.ui.screen.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.model.ReportEntityType
import com.chosv.chosv_android.data.repository.FavoriteRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import com.chosv.chosv_android.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val productDetail: ProductDetail? = null,
    val popularProducts: List<Product> = emptyList(),
    val newestProducts: List<Product> = emptyList(),
    val similarProducts: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Report state
    val showReportDialog: Boolean = false,
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false
)

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reportRepository: ReportRepository,
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
                // Chạy song song 4 yêu cầu mạng
                val detail = productRepository.getProductDetail(productId)
                val popularResponse = productRepository.getPopularProducts(page = 1, pageSize = 10, daysBack = 100)
                val newestResponse = productRepository.getNewestProducts(page = 1, pageSize = 10)
                val similarResponse = productRepository.getSimilarProducts(productId = productId, page = 1, pageSize = 10)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        productDetail = detail,
                        popularProducts = popularResponse.items,
                        newestProducts = newestResponse.items,
                        similarProducts = similarResponse.items
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
            val updatedSimilar = currentState.similarProducts.map { product ->
                if (product.productId == productId) {
                    product.copy(isFavorited = !product.isFavorited)
                } else {
                    product
                }
            }
            currentState.copy(
                popularProducts = updatedPopular,
                newestProducts = updatedNewest,
                similarProducts = updatedSimilar
            )
        }
    }

    // Report functions
    fun showReportDialog() {
        _uiState.update { it.copy(showReportDialog = true) }
    }

    fun hideReportDialog() {
        _uiState.update { it.copy(showReportDialog = false) }
    }

    fun reportProduct(reason: String) {
        val productDetail = _uiState.value.productDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true) }
            try {
                reportRepository.sendReport(
                    entityId = productDetail.productId.toString(),
                    entityType = ReportEntityType.Product,
                    reason = reason
                )
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        showReportDialog = false,
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            favoriteRepository: FavoriteRepository,
            reportRepository: ReportRepository,
            productId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(productRepository, favoriteRepository, reportRepository, productId) as T
            }
        }
    }
}
