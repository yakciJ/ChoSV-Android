package com.chosv.chosv_android.ui.screen.management

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.convertBaseUrl
// sao cái giao diện này và cả cái đăng sản phẩm, bên trên của cái Đăng Sản phẩm hay Chỉnh sửa sản phẩm lại có 1 dòng trống nhỉ?
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProductScreen(
    productId: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: EditProductViewModel = viewModel(
        factory = EditProductViewModel.provideFactory(
            productRepository = application.container.productRepository,
            categoryRepository = application.container.categoryRepository,
            imageRepository = application.container.imageRepository,
            productId = productId
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addNewImages(uris)
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success dialog
    if (uiState.successMessage != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Thành công") },
            text = { Text(uiState.successMessage ?: "") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSuccess()
                        navController.popBackStack()
                    }
                ) {
                    Text("Quay lại")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa sản phẩm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoadingProduct || uiState.isLoadingCategories -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.productDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không tìm thấy sản phẩm")
                }
            }
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- Ảnh sản phẩm ---
                    Text(
                        text = "Ảnh sản phẩm (tối đa 6 ảnh)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val totalImages = uiState.existingImageUrls.size + uiState.newImageUris.size

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Nút thêm ảnh
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (uiState.imageError != null) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (totalImages < 6) {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Thêm ảnh",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$totalImages/6",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // Hiển thị ảnh đã có trên server
                        items(uiState.existingImageUrls) { url ->
                            ExistingImageThumbnail(
                                imageUrl = url,
                                onRemove = { viewModel.removeExistingImage(url) }
                            )
                        }

                        // Hiển thị ảnh mới chọn
                        items(uiState.newImageUris) { uri ->
                            ImageThumbnail(
                                imageUri = uri,
                                onRemove = { viewModel.removeNewImage(uri) }
                            )
                        }
                    }

                    if (uiState.imageError != null) {
                        Text(
                            text = uiState.imageError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // --- Tên sản phẩm ---
                    OutlinedTextField(
                        value = uiState.productName,
                        onValueChange = { viewModel.updateProductName(it) },
                        label = { Text("Tên sản phẩm *") },
                        placeholder = { Text("Nhập tên sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.productNameError != null,
                        supportingText = {
                            if (uiState.productNameError != null) {
                                Text(uiState.productNameError!!)
                            }
                        },
                        singleLine = true
                    )

                    // --- Giá sản phẩm ---
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = { Text("Giá (VNĐ) *") },
                        placeholder = { Text("Nhập giá sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.priceError != null,
                        supportingText = {
                            if (uiState.priceError != null) {
                                Text(uiState.priceError!!)
                            }
                        },
                        singleLine = true
                    )

                    // --- Danh mục ---
                    Text(
                        text = "Danh mục *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    CategoryDropdown(
                        label = "Chọn danh mục",
                        categories = uiState.categoryTree,
                        selectedCategory = uiState.selectedParentCategory,
                        onCategorySelected = { viewModel.selectParentCategory(it) },
                        isError = uiState.categoryError != null && uiState.selectedParentCategory == null
                    )

                    // Dropdown chọn danh mục con
                    if (uiState.selectedParentCategory != null &&
                        uiState.selectedParentCategory!!.childs.isNotEmpty()
                    ) {
                        Text(
                            text = "Danh mục con *",
                            style = MaterialTheme.typography.titleSmall
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.selectedParentCategory!!.childs.forEach { childCategory ->
                                FilterChip(
                                    selected = uiState.selectedChildCategory?.categoryId == childCategory.categoryId,
                                    onClick = { viewModel.selectChildCategory(childCategory) },
                                    label = { Text(childCategory.name) },
                                    leadingIcon = {
                                        if (uiState.selectedChildCategory?.categoryId == childCategory.categoryId) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.categoryError != null) {
                        Text(
                            text = uiState.categoryError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // --- Mô tả sản phẩm ---
                    OutlinedTextField(
                        value = uiState.productDescription,
                        onValueChange = { viewModel.updateProductDescription(it) },
                        label = { Text("Mô tả sản phẩm") },
                        placeholder = { Text("Nhập mô tả chi tiết về sản phẩm...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Nút cập nhật ---
                    Button(
                        onClick = { viewModel.submitUpdate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isSubmitting && !uiState.isUploadingImages
                    ) {
                        if (uiState.isSubmitting || uiState.isUploadingImages) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isUploadingImages) "Đang tải ảnh..."
                                else "Đang cập nhật..."
                            )
                        } else {
                            Text("Cập nhật sản phẩm", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ExistingImageThumbnail(
    imageUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(100.dp)) {
        Image(
            painter = rememberAsyncImagePainter(model = convertBaseUrl(imageUrl)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // Nút xóa
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Xóa ảnh",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

