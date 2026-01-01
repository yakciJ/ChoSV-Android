package com.chosv.chosv_android.ui.screen.management

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.model.UpdateProductRequest
import com.chosv.chosv_android.data.repository.CategoryRepository
import com.chosv.chosv_android.data.repository.ImageRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProductUiState(
    // Product data
    val productId: Int = 0,
    val productDetail: ProductDetail? = null,

    // Form fields
    val productName: String = "",
    val productDescription: String = "",
    val price: String = "",

    // Category selection
    val categoryTree: List<CategoryTree> = emptyList(),
    val selectedParentCategory: CategoryTree? = null,
    val selectedChildCategory: CategoryTree? = null,

    // Images - URLs đã có trên server
    val existingImageUrls: List<String> = emptyList(),
    // Images - Ảnh mới chọn từ thiết bị
    val newImageUris: List<Uri> = emptyList(),
    val isUploadingImages: Boolean = false,

    // UI states
    val isLoadingProduct: Boolean = true,
    val isLoadingCategories: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    // Validation
    val productNameError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null,
    val imageError: String? = null
)

class EditProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository,
    private val productId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState(productId = productId))
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    init {
        loadProductAndCategories()
    }

    private fun loadProductAndCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProduct = true, isLoadingCategories = true) }

            try {
                // Load categories
                val categories = categoryRepository.getCategoryTree()

                // Load product detail
                val product = productRepository.getProductDetail(productId)

                // Tìm parent và child category dựa trên product
                val parentCategory = categories.find { it.categoryId == product.parentCategoryId }
                val childCategory = parentCategory?.childs?.find { it.categoryId == product.childCategoryId }

                _uiState.update {
                    it.copy(
                        isLoadingProduct = false,
                        isLoadingCategories = false,
                        productDetail = product,
                        productName = product.productName,
                        productDescription = product.productDescription,
                        price = product.price.toLong().toString(),
                        categoryTree = categories,
                        selectedParentCategory = parentCategory,
                        selectedChildCategory = childCategory,
                        existingImageUrls = product.productImages
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingProduct = false,
                        isLoadingCategories = false,
                        error = "Không thể tải dữ liệu: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateProductName(name: String) {
        _uiState.update {
            it.copy(
                productName = name,
                productNameError = if (name.isBlank()) "Vui lòng nhập tên sản phẩm" else null
            )
        }
    }

    fun updateProductDescription(description: String) {
        _uiState.update { it.copy(productDescription = description) }
    }

    fun updatePrice(price: String) {
        val filteredPrice = price.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                price = filteredPrice,
                priceError = if (filteredPrice.isBlank() || filteredPrice.toLongOrNull() == null || filteredPrice.toLong() <= 0) {
                    "Vui lòng nhập giá hợp lệ"
                } else null
            )
        }
    }

    fun selectParentCategory(category: CategoryTree) {
        _uiState.update {
            it.copy(
                selectedParentCategory = category,
                selectedChildCategory = null,
                categoryError = null
            )
        }
    }

    fun selectChildCategory(category: CategoryTree) {
        _uiState.update {
            it.copy(
                selectedChildCategory = category,
                categoryError = null
            )
        }
    }

    fun addNewImages(uris: List<Uri>) {
        val currentTotal = _uiState.value.existingImageUrls.size + _uiState.value.newImageUris.size
        val availableSlots = 6 - currentTotal
        val newImages = (_uiState.value.newImageUris + uris).takeLast(
            minOf(_uiState.value.newImageUris.size + uris.size, availableSlots + _uiState.value.newImageUris.size)
        )

        _uiState.update {
            it.copy(
                newImageUris = newImages.take(6 - it.existingImageUrls.size),
                imageError = null
            )
        }
    }

    fun removeNewImage(uri: Uri) {
        _uiState.update {
            it.copy(newImageUris = it.newImageUris - uri)
        }
    }

    fun removeExistingImage(url: String) {
        _uiState.update {
            it.copy(existingImageUrls = it.existingImageUrls - url)
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        if (_uiState.value.productName.isBlank()) {
            _uiState.update { it.copy(productNameError = "Vui lòng nhập tên sản phẩm") }
            isValid = false
        }

        val priceValue = _uiState.value.price.toLongOrNull()
        if (priceValue == null || priceValue <= 0) {
            _uiState.update { it.copy(priceError = "Vui lòng nhập giá hợp lệ") }
            isValid = false
        }

        val parent = _uiState.value.selectedParentCategory
        val child = _uiState.value.selectedChildCategory
        if (parent == null) {
            _uiState.update { it.copy(categoryError = "Vui lòng chọn danh mục") }
            isValid = false
        } else if (parent.childs.isNotEmpty() && child == null) {
            _uiState.update { it.copy(categoryError = "Vui lòng chọn danh mục con") }
            isValid = false
        }

        val totalImages = _uiState.value.existingImageUrls.size + _uiState.value.newImageUris.size
        if (totalImages == 0) {
            _uiState.update { it.copy(imageError = "Vui lòng thêm ít nhất 1 ảnh") }
            isValid = false
        }

        return isValid
    }

    fun submitUpdate() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            try {
                // Bắt đầu với các URL ảnh đã có
                val allImageUrls = _uiState.value.existingImageUrls.toMutableList()

                // Upload ảnh mới nếu có
                if (_uiState.value.newImageUris.isNotEmpty()) {
                    _uiState.update { it.copy(isUploadingImages = true) }

                    val response = imageRepository.uploadMultipleImages(_uiState.value.newImageUris)
                    allImageUrls.addAll(response.imageUrls)

                    _uiState.update { it.copy(isUploadingImages = false) }
                }

                // Xác định category ID
                val categoryId = _uiState.value.selectedChildCategory?.categoryId
                    ?: _uiState.value.selectedParentCategory?.categoryId
                    ?: throw Exception("Không có danh mục được chọn")

                // Tạo request
                val request = UpdateProductRequest(
                    productName = _uiState.value.productName.trim(),
                    productDescription = _uiState.value.productDescription.trim(),
                    price = _uiState.value.price.toLong(),
                    categoryIds = listOf(categoryId),
                    imageUrls = allImageUrls
                )

                val response = productRepository.updateProduct(productId, request)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = response.message
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isUploadingImages = false,
                        error = "Cập nhật thất bại: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            imageRepository: ImageRepository,
            productId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditProductViewModel(
                    productRepository,
                    categoryRepository,
                    imageRepository,
                    productId
                ) as T
            }
        }
    }
}

