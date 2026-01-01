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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.data.model.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: CreatePostViewModel = viewModel(
        factory = CreatePostViewModel.provideFactory(
            productRepository = application.container.productRepository,
            categoryRepository = application.container.categoryRepository,
            imageRepository = application.container.imageRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addImages(uris)
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
                        viewModel.resetForm()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.CreatePost.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Về trang chủ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSuccess()
                        viewModel.resetForm()
                    }
                ) {
                    Text("Đăng bài mới")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Đăng sản phẩm", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoadingCategories) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                                    if (uiState.selectedImageUris.size < 6) {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm ảnh",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${uiState.selectedImageUris.size}/6",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Hiển thị ảnh đã chọn
                    items(uiState.selectedImageUris) { uri ->
                        ImageThumbnail(
                            imageUri = uri,
                            onRemove = { viewModel.removeImage(uri) }
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

                // Dropdown chọn danh mục cha
                CategoryDropdown(
                    label = "Chọn danh mục",
                    categories = uiState.categoryTree,
                    selectedCategory = uiState.selectedParentCategory,
                    onCategorySelected = { viewModel.selectParentCategory(it) },
                    isError = uiState.categoryError != null && uiState.selectedParentCategory == null
                )

                // Dropdown chọn danh mục con (nếu có)
                if (uiState.selectedParentCategory != null &&
                    uiState.selectedParentCategory!!.childs.isNotEmpty()
                ) {
                    Text(
                        text = "Danh mục con *",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Hiển thị các chip để chọn danh mục con
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

                // --- Nút đăng bài ---
                Button(
                    onClick = { viewModel.submitPost() },
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
                            else "Đang đăng..."
                        )
                    } else {
                        Text("Đăng sản phẩm", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(100.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
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

@Composable
fun CategoryDropdown(
    label: String,
    categories: List<CategoryTree>,
    selectedCategory: CategoryTree?,
    onCategorySelected: (CategoryTree) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text("Chọn danh mục") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Mở dropdown"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.name)
                            if (category.childs.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${category.childs.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

