package com.chosv.chosv_android.ui.screen.management

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.data.model.CreateProductRequest
import com.chosv.chosv_android.data.repository.CategoryRepository
import com.chosv.chosv_android.data.repository.ImageRepository
import com.chosv.chosv_android.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePostUiState(
    // Form fields
    val productName: String = "",
    val productDescription: String = "",
    val price: String = "",

    // Category selection
    val categoryTree: List<CategoryTree> = emptyList(),
    val selectedParentCategory: CategoryTree? = null,
    val selectedChildCategory: CategoryTree? = null,

    // Images
    val selectedImageUris: List<Uri> = emptyList(),
    val uploadedImageUrls: List<String> = emptyList(),
    val isUploadingImages: Boolean = false,

    // UI states
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

class CreatePostViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            try {
                val tree = categoryRepository.getCategoryTree()
                _uiState.update {
                    it.copy(
                        categoryTree = tree,
                        isLoadingCategories = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể tải danh mục: ${e.message}",
                        isLoadingCategories = false
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
        // Chỉ cho phép nhập số
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
                selectedChildCategory = null, // Reset child khi đổi parent
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

    fun addImages(uris: List<Uri>) {
        val currentImages = _uiState.value.selectedImageUris
        val newImages = (currentImages + uris).take(6) // Tối đa 6 ảnh
        _uiState.update {
            it.copy(
                selectedImageUris = newImages,
                imageError = null
            )
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update {
            it.copy(selectedImageUris = it.selectedImageUris - uri)
        }
    }

    fun removeUploadedImage(url: String) {
        _uiState.update {
            it.copy(uploadedImageUrls = it.uploadedImageUrls - url)
        }
        // Optionally delete from server
        viewModelScope.launch {
            try {
                imageRepository.deleteImage(url)
            } catch (_: Exception) {
                // Ignore delete errors
            }
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

        // Kiểm tra category: nếu parent có children thì phải chọn child
        val parent = _uiState.value.selectedParentCategory
        val child = _uiState.value.selectedChildCategory
        if (parent == null) {
            _uiState.update { it.copy(categoryError = "Vui lòng chọn danh mục") }
            isValid = false
        } else if (parent.childs.isNotEmpty() && child == null) {
            _uiState.update { it.copy(categoryError = "Vui lòng chọn danh mục con") }
            isValid = false
        }

        if (_uiState.value.selectedImageUris.isEmpty() && _uiState.value.uploadedImageUrls.isEmpty()) {
            _uiState.update { it.copy(imageError = "Vui lòng thêm ít nhất 1 ảnh") }
            isValid = false
        }

        return isValid
    }

    fun submitPost() {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            try {
                // Step 1: Upload images if there are local images
                val uploadedUrls = _uiState.value.uploadedImageUrls.toMutableList()

                if (_uiState.value.selectedImageUris.isNotEmpty()) {
                    _uiState.update { it.copy(isUploadingImages = true) }

                    val response = imageRepository.uploadMultipleImages(_uiState.value.selectedImageUris)
                    uploadedUrls.addAll(response.imageUrls)

                    _uiState.update { it.copy(isUploadingImages = false) }
                }

                // Step 2: Determine category ID to send
                // Nếu có child category thì gửi child, không thì gửi parent
                val categoryId = _uiState.value.selectedChildCategory?.categoryId
                    ?: _uiState.value.selectedParentCategory?.categoryId
                    ?: throw Exception("Không có danh mục được chọn")

                // Step 3: Create product
                val request = CreateProductRequest(
                    productName = _uiState.value.productName.trim(),
                    productDescription = _uiState.value.productDescription.trim(),
                    price = _uiState.value.price.toLong(),
                    categoryIds = listOf(categoryId),
                    imageUrls = uploadedUrls
                )

                val response = productRepository.createProduct(request)

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
                        error = "Đăng bài thất bại: ${e.message}"
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

    fun resetForm() {
        _uiState.update {
            CreatePostUiState(
                categoryTree = it.categoryTree // Giữ lại category tree
            )
        }
    }

    companion object {
        fun provideFactory(
            productRepository: ProductRepository,
            categoryRepository: CategoryRepository,
            imageRepository: ImageRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreatePostViewModel(
                    productRepository,
                    categoryRepository,
                    imageRepository
                ) as T
            }
        }
    }
}

