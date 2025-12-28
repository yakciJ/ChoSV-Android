package com.chosv.chosv_android.ui.screen.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
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

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            productId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(productRepository, productId) as T
            }
        }
    }
}
