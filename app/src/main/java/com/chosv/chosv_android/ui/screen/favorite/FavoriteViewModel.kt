package com.chosv.chosv_android.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val products: List<Product> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        loadFavorites(1)
    }

    fun loadFavorites(page: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = favoriteRepository.getFavorites(page = page, pageSize = pageSize)
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

    fun goToNextPage() {
        if (_uiState.value.hasNext) {
            loadFavorites(_uiState.value.currentPage + 1)
        }
    }

    fun goToPreviousPage() {
        if (_uiState.value.hasPrevious) {
            loadFavorites(_uiState.value.currentPage - 1)
        }
    }

    fun goToPage(page: Int) {
        if (page in 1.._uiState.value.totalPages) {
            loadFavorites(page)
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
                // Sau khi toggle, reload lại trang hiện tại
                loadFavorites(_uiState.value.currentPage)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi thao tác: ${e.message}") }
            }
        }
    }

    companion object {
        fun provideFactory(
            favoriteRepository: FavoriteRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoriteViewModel(favoriteRepository) as T
            }
        }
    }
}

