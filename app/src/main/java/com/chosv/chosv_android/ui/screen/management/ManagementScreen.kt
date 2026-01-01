package com.chosv.chosv_android.ui.screen.management

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.convertBaseUrl
import com.chosv.chosv_android.data.model.MyProduct
import com.chosv.chosv_android.formatCurrency

@Composable
fun ManagementScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: ManagementViewModel = viewModel(
        factory = ManagementViewModel.provideFactory(
            productRepository = application.container.productRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Hiển thị success snackbar
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    // Dialog xác nhận xóa
    if (uiState.showDeleteDialog && uiState.productToDelete != null) {
        DeleteConfirmDialog(
            productName = uiState.productToDelete!!.productName,
            isDeleting = uiState.isDeleting,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    // Dialog đổi trạng thái
    if (uiState.showStatusDialog && uiState.productToChangeStatus != null) {
        StatusChangeDialog(
            product = uiState.productToChangeStatus!!,
            isUpdating = uiState.isUpdatingStatus,
            onStatusChange = { newStatus -> viewModel.changeStatus(newStatus) },
            onDismiss = { viewModel.dismissStatusDialog() }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ManagementHeader(
                displayedCount = uiState.products.size,
                totalCount = uiState.totalCount
            )

            // Tabs
            StatusTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

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
                uiState.products.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f)) {
                        EmptyProductState()
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.products, key = { it.productId }) { product ->
                            ProductManagementCard(
                                product = product,
                                onEditClick = {
                                    navController.navigate("edit_product/${product.productId}")
                                },
                                onStatusClick = { viewModel.showStatusDialog(product) },
                                onDeleteClick = { viewModel.showDeleteConfirmation(product) }
                            )
                        }

                        // Phân trang ở cuối danh sách
                        if (uiState.totalPages > 1) {
                            item {
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

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ManagementHeader(
    displayedCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sản phẩm của bạn",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$displayedCount/$totalCount",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusTabRow(
    selectedTab: ProductStatusTab,
    onTabSelected: (ProductStatusTab) -> Unit
) {
    val tabs = ProductStatusTab.entries.toList()
    val selectedIndex = tabs.indexOf(selectedTab)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp
    ) {
        tabs.forEach { statusTab ->
            Tab(
                selected = statusTab == selectedTab,
                onClick = { onTabSelected(statusTab) },
                text = {
                    Text(
                        text = statusTab.displayName,
                        fontWeight = if (statusTab == selectedTab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun ProductManagementCard(
    product: MyProduct,
    onEditClick: () -> Unit,
    onStatusClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Chiều cao cố định cho card để đảm bảo 3 nút luôn vừa
    val cardHeight = 120.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh sản phẩm - chiều cao = chiều cao card, chiều rộng = chiều cao
            Image(
                painter = rememberAsyncImagePainter(model = convertBaseUrl(product.firstImageUrl)),
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin sản phẩm
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatCurrency(product.productPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                StatusBadge(status = product.status)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons - luôn có chiều cao cố định cho 3 nút
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Status button - luôn hiển thị nhưng ẩn đi nếu không phải Approved/Sold
                val showStatusButton = product.status == "Approved" || product.status == "Sold"
                IconButton(
                    onClick = onStatusClick,
                    modifier = Modifier
                        .size(32.dp)
                        .then(if (!showStatusButton) Modifier.alpha(0f) else Modifier),
                    enabled = showStatusButton,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Đổi trạng thái",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, displayText) = when (status) {
        "Pending" -> Triple(Color(0xFFFFF3CD), Color(0xFF856404), "Chờ duyệt")
        "Approved" -> Triple(Color(0xFFD4EDDA), Color(0xFF155724), "Đang bán")
        "Rejected" -> Triple(Color(0xFFF8D7DA), Color(0xFF721C24), "Bị từ chối")
        "Sold" -> Triple(Color(0xFFD1ECF1), Color(0xFF0C5460), "Đã bán")
        else -> Triple(Color.Gray, Color.White, status)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyProductState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có sản phẩm nào",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
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
private fun DeleteConfirmDialog(
    productName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text("Xác nhận xóa") },
        text = {
            Text("Bạn có chắc chắn muốn xóa sản phẩm \"$productName\"? Hành động này không thể hoàn tác.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun StatusChangeDialog(
    product: MyProduct,
    isUpdating: Boolean,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val newStatus = if (product.status == "Approved") "Sold" else "Approved"
    val newStatusDisplay = if (newStatus == "Sold") "Đã bán" else "Đang bán"

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = { Text("Đổi trạng thái") },
        text = {
            Text("Chuyển sản phẩm \"${product.productName}\" sang trạng thái \"$newStatusDisplay\"?")
        },
        confirmButton = {
            TextButton(
                onClick = { onStatusChange(newStatus) },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Xác nhận")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Hủy")
            }
        }
    )
}
