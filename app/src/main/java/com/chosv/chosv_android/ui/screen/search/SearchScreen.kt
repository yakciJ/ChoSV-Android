package com.chosv.chosv_android.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.provideFactory(
            productRepository = application.container.productRepository,
            categoryRepository = application.container.categoryRepository,
            favoriteRepository = application.container.favoriteRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState()

    // Auto focus vào ô search khi mở màn hình
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Hiển thị error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Header
        SearchHeader(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = {
                keyboardController?.hide()
                viewModel.performSearch()
            },
            onBackClick = { navController.popBackStack() },
            onFilterClick = { viewModel.showFilterSheet() },
            focusRequester = focusRequester
        )

        // Sort Options Row
        SortOptionsRow(
            currentSort = uiState.sortBy,
            onSortChange = { viewModel.updateSortBy(it) },
            hasActiveFilters = uiState.selectedCategoryId != null ||
                    uiState.minPrice != null ||
                    uiState.maxPrice != null
        )

        // Results Header
        if (uiState.hasSearched && uiState.searchQuery.isNotBlank()) {
            ResultsHeader(
                searchQuery = uiState.searchQuery,
                displayedCount = uiState.products.size,
                totalCount = uiState.totalCount
            )
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Đã có lỗi xảy ra: ${uiState.error}")
                }
            }
            !uiState.hasSearched -> {
                // Chưa tìm kiếm gì
                EmptySearchState(message = "Nhập từ khóa để tìm kiếm sản phẩm")
            }
            uiState.products.isEmpty() -> {
                // Không tìm thấy kết quả
                EmptySearchState(message = "Không tìm thấy sản phẩm nào cho \"${uiState.searchQuery}\"")
            }
            else -> {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.products, key = { it.productId }) { product ->
                        ProductCard(
                            product = product,
                            onCardClick = { navController.navigate("products/${product.productId}") },
                            onFavoriteClick = {
                                viewModel.toggleFavorite(product.productId, product.isFavorited)
                            }
                        )
                    }

                    // Phân trang ở cuối grid
                    if (uiState.totalPages > 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PaginationControls(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                hasPrevious = uiState.hasPrevious,
                                hasNext = uiState.hasNext,
                                onPreviousClick = { viewModel.goToPreviousPage() },
                                onNextClick = { viewModel.goToNextPage() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (uiState.showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideFilterSheet() },
            sheetState = sheetState
        ) {
            FilterSheetContent(
                categories = uiState.categories,
                selectedCategoryName = uiState.selectedCategoryName,
                minPrice = uiState.minPrice,
                maxPrice = uiState.maxPrice,
                onCategorySelect = { id, name -> viewModel.updateCategory(id, name) },
                onMinPriceChange = { viewModel.updateMinPrice(it) },
                onMaxPriceChange = { viewModel.updateMaxPrice(it) },
                onApply = { viewModel.applyFilters() },
                onClear = { viewModel.clearFilters() }
            )
        }
    }
}

@Composable
private fun SearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút quay lại
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại"
            )
        }

        // Ô tìm kiếm
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text("Tìm kiếm sản phẩm...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Xóa"
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            )
        )

        // Nút lọc
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Lọc"
            )
        }
    }
}

@Composable
private fun SortOptionsRow(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    hasActiveFilters: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sắp xếp:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = { Text(currentSort.displayName) }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = {
                            onSortChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (hasActiveFilters) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("Đang lọc") }
            )
        }
    }
}

@Composable
private fun ResultsHeader(
    searchQuery: String,
    displayedCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Kết quả tìm kiếm cho \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (totalCount > 0) {
            Text(
                text = "($displayedCount/$totalCount)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptySearchState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousClick,
            enabled = hasPrevious,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (hasPrevious) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Trang trước"
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Trang $currentPage / $totalPages",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onNextClick,
            enabled = hasNext,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (hasNext) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Trang sau"
            )
        }
    }
}

@Composable
private fun FilterSheetContent(
    categories: List<CategoryTree>,
    selectedCategoryName: String?,
    minPrice: Double?,
    maxPrice: Double?,
    onCategorySelect: (Int?, String?) -> Unit,
    onMinPriceChange: (Double?) -> Unit,
    onMaxPriceChange: (Double?) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    var minPriceText by remember(minPrice) { mutableStateOf(minPrice?.toLong()?.toString() ?: "") }
    var maxPriceText by remember(maxPrice) { mutableStateOf(maxPrice?.toLong()?.toString() ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Bộ lọc tìm kiếm",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Danh mục
        Text(
            text = "Danh mục",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = selectedCategoryName ?: "Tất cả danh mục",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDropdown = true },
                readOnly = true,
                enabled = false
            )

            // Invisible clickable overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showCategoryDropdown = true }
            )

            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tất cả danh mục") },
                    onClick = {
                        onCategorySelect(null, null)
                        showCategoryDropdown = false
                    }
                )
                HorizontalDivider()
                categories.forEach { parentCategory ->
                    // Parent category
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = parentCategory.name,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = {
                            onCategorySelect(parentCategory.categoryId, parentCategory.name)
                            showCategoryDropdown = false
                        }
                    )
                    // Child categories
                    parentCategory.childs.forEach { childCategory ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "  └ ${childCategory.name}",
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            },
                            onClick = {
                                onCategorySelect(childCategory.categoryId, childCategory.name)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Khoảng giá
        Text(
            text = "Khoảng giá",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = minPriceText,
                onValueChange = { value ->
                    minPriceText = value.filter { it.isDigit() }
                    onMinPriceChange(minPriceText.toDoubleOrNull())
                },
                modifier = Modifier.weight(1f),
                label = { Text("Giá thấp nhất") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("đ") }
            )

            OutlinedTextField(
                value = maxPriceText,
                onValueChange = { value ->
                    maxPriceText = value.filter { it.isDigit() }
                    onMaxPriceChange(maxPriceText.toDoubleOrNull())
                },
                modifier = Modifier.weight(1f),
                label = { Text("Giá cao nhất") },
                placeholder = { Text("∞") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("đ") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    minPriceText = ""
                    maxPriceText = ""
                    onClear()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xóa bộ lọc")
            }

            TextButton(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text("Áp dụng")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

