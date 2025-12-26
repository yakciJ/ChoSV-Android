package com.chosv.chosv_android.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.ui.components.ProductCard

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProductClick: (Int) -> Unit
) {
    // Lấy Application context để truy cập vào AppContainer
    val application = LocalContext.current.applicationContext as ChoSVApplication

    // Khởi tạo ViewModel và truyền vào factory đã tạo ở trên
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(application.container.productRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Xử lý các trạng thái UI: Đang tải, Lỗi, Thành công
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Đã có lỗi xảy ra: ${uiState.error}")
        }
    } else {
        // Chỉ hiển thị danh sách sản phẩm khi đã tải xong và không có lỗi
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Fixed(2), // 2 cột
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Phần Sản phẩm nổi bật ---
            if (uiState.popularProducts.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { // maxLineSpan để chiếm hết chiều rộng
                    SectionHeader(
                        title = "Sản phẩm nổi bật",
                        onSeeMoreClick = { /* TODO: Điều hướng đến trang xem tất cả sp nổi bật */ }
                    )
                }
                items(uiState.popularProducts) { product ->
                    ProductCard(
                        product = product,
                        onCardClick = { onProductClick(product.productId) },
                        onFavoriteClick = { /* TODO: Xử lý sự kiện yêu thích */ }
                    )
                }
            }

            // --- Phần Sản phẩm mới nhất ---
            if (uiState.newestProducts.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(
                        title = "Khám phá sản phẩm mới",
                        onSeeMoreClick = { /* TODO: Điều hướng đến trang xem tất cả sp mới */ }
                    )
                }
                items(uiState.newestProducts) { product ->
                    ProductCard(
                        product = product,
                        onCardClick = { onProductClick(product.productId) },
                        onFavoriteClick = { /* TODO: XÃử lý sự kiện yêu thích */ }
                    )
                }
            }
        }
    }
}

// Component nhỏ cho tiêu đề của mỗi khu vực (ví dụ: "Sản phẩm nổi bật")
@Composable
private fun SectionHeader(
    title: String,
    onSeeMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp), // Thêm padding top để có khoảng cách
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSeeMoreClick() }
        ) {
            Text(
                text = "Xem thêm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Xem thêm",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}