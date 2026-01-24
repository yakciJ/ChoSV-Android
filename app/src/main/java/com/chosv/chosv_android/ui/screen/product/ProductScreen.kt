package com.chosv.chosv_android.ui.screen.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.convertBaseUrl
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.data.model.ProductDetail
import com.chosv.chosv_android.data.model.ReportEntityType
import com.chosv.chosv_android.formatCurrency
import com.chosv.chosv_android.formatDateString
import com.chosv.chosv_android.ui.components.ProductCard
import com.chosv.chosv_android.ui.components.ReportDialog
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme
import androidx.navigation.NavHostController
// cái ảnh to phải full ảnh, và ấn vào phải kiểu phóng to cái ảnh ra để xem rõ hơn..
@Composable
fun ProductScreen(
    productId: Int,
    onBack: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.provideFactory(
            productRepository = application.container.productRepository,
            favoriteRepository = application.container.favoriteRepository,
            reportRepository = application.container.reportRepository,
            productId = productId
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị snackbar khi báo cáo thành công
    LaunchedEffect(uiState.reportSuccess) {
        if (uiState.reportSuccess) {
            snackbarHostState.showSnackbar("Báo cáo đã được gửi thành công!")
            viewModel.clearReportSuccess()
        }
    }

    // Hiển thị snackbar khi có lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Report Dialog
    if (uiState.showReportDialog && uiState.productDetail != null) {
        ReportDialog(
            entityType = ReportEntityType.Product,
            entityName = uiState.productDetail!!.productName,
            isLoading = uiState.isReporting,
            onDismiss = { viewModel.hideReportDialog() },
            onSubmit = { reason -> viewModel.reportProduct(reason) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null && uiState.productDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: ${uiState.error}")
            }
        } else {
            uiState.productDetail?.let {
                ProductDetailContent(
                    product = it,
                    newestProducts = uiState.newestProducts,
                    popularProducts = uiState.popularProducts,
                    similarProducts = uiState.similarProducts,
                    onBackClick = onBack,
                    onProductClick = { clickedProductId -> navController.navigate("products/$clickedProductId") },
                    onFavoriteClick = { clickedProductId, isFavorited ->
                        viewModel.toggleFavorite(clickedProductId, isFavorited)
                    },
                    onContactSellerClick = { sellerId, sellerName ->
                        navController.navigate("chat/$sellerId/${java.net.URLEncoder.encode(sellerName, "UTF-8")}")
                    },
                    onViewSellerProfileClick = { sellerName ->
                        navController.navigate("user_profile/${java.net.URLEncoder.encode(sellerName, "UTF-8")}")
                    },
                    onViewMoreSimilar = {
                        val encodedName = java.net.URLEncoder.encode(it.productName, "UTF-8")
                        navController.navigate("browsing/similar/${it.productId}/$encodedName")
                    },
                    onViewMorePopular = {
                        navController.navigate("browsing/popular")
                    },
                    onViewMoreNewest = {
                        navController.navigate("browsing/newest")
                    },
                    onReportClick = { viewModel.showReportDialog() }
                )
            }
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ProductDetailContent(
    product: ProductDetail,
    newestProducts: List<Product>,
    popularProducts: List<Product>,
    similarProducts: List<Product>,
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onContactSellerClick: (sellerId: String, sellerName: String) -> Unit,
    onViewSellerProfileClick: (sellerName: String) -> Unit,
    onViewMoreSimilar: () -> Unit,
    onViewMorePopular: () -> Unit,
    onViewMoreNewest: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImage by remember { mutableStateOf(product.productImages.firstOrNull()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header với nút quay lại và tiêu đề
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
            Text(
                text = product.productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            // Report button
            IconButton(onClick = onReportClick) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Báo cáo sản phẩm",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Image(
            painter = rememberAsyncImagePainter(model = convertBaseUrl(selectedImage)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable { /* TODO: Handle image zoom */ },
            contentScale = ContentScale.Crop
        )

        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(product.productImages) { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = convertBaseUrl(imageUrl)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { selectedImage = imageUrl },
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = formatCurrency(product.price),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Ngày đăng: ${formatDateString(product.createdDate)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
            onClick = { onContactSellerClick(product.sellerId, product.sellerName) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Liên hệ người bán")
        }

        // Seller Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.sellerAvatarImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = convertBaseUrl(product.sellerAvatarImage)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.sellerFullName, fontWeight = FontWeight.Bold)
                Text(text = "Tham gia từ ${formatDateString(product.sellerJoinedDate)}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { onViewSellerProfileClick(product.sellerName) }) {
                Text(text = "Xem trang")
            }
        }

        // --- Additional Seller Info ---
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (product.sellerEmail.isNotEmpty()) {
                InfoRow(icon = Icons.Default.Email, text = product.sellerEmail)
            }
            if (product.sellerPhone.isNotEmpty()) {
                InfoRow(icon = Icons.Default.Phone, text = product.sellerPhone)
            }
            if (product.sellerAddress.isNotEmpty()) {
                InfoRow(icon = Icons.Default.Business, text = product.sellerAddress)
            }
        }


        Text(
            text = "Mô tả chi tiết",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = product.productDescription,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Similar Products ---
        if (similarProducts.isNotEmpty()) {
            ProductCarousel(
                title = "Sản phẩm tương tự",
                products = similarProducts,
                onProductClick = onProductClick,
                onFavoriteClick = onFavoriteClick,
                onViewMore = onViewMoreSimilar
            )
        }

        // --- Related Products ---
        ProductCarousel(
            title = "Sản phẩm nổi bật",
            products = popularProducts,
            onProductClick = onProductClick,
            onFavoriteClick = onFavoriteClick,
            onViewMore = onViewMorePopular
        )
        ProductCarousel(
            title = "Sản phẩm mới nhất",
            products = newestProducts,
            onProductClick = onProductClick,
            onFavoriteClick = onFavoriteClick,
            onViewMore = onViewMoreNewest
        )

    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ProductCarousel(
    title: String,
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onViewMore: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (onViewMore != null) {
                Text(
                    text = "Xem thêm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewMore() }
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onCardClick = { onProductClick(product.productId) },
                    onFavoriteClick = { onFavoriteClick(product.productId, product.isFavorited) },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    ChoSVAndroidTheme {
        ProductDetailContent(
            product = ProductDetail(
                productId = 1,
                productName = "Sản phẩm mẫu",
                sellerId = "1",
                sellerName = "Người bán mẫu",
                sellerAddress = "123 Đường ABC, Quận 1, TP.HCM",
                sellerPhone = "0123456789",
                productDescription = "Đây là mô tả chi tiết cho sản phẩm mẫu. Mô tả này khá dài để kiểm tra xem giao diện có bị vỡ hay không.",
                price = 100000.0,
                status = "Mới",
                createdDate = "2024-07-28T10:00:00.000000",
                sellerFullName = "Nguyễn Văn A",
                sellerAvatarImage = null,
                sellerEmail = "nguyenvana@example.com",
                sellerJoinedDate = "2024-01-01T10:00:00.000000",
                productImages = listOf("", "", "", "", "", "", ""),
                favoriteCount = 10,
                isFavorite = false,
                parentCategoryId = 1,
                parentCategoryName = "Danh mục cha",
                childCategoryId = 101,
                childCategoryName = "Danh mục con"
            ),
            newestProducts = emptyList(),
            popularProducts = emptyList(),
            similarProducts = emptyList(),
            onProductClick = {},
            onBackClick = {},
            onFavoriteClick = { _, _ -> },
            onContactSellerClick = { _, _ -> },
            onViewSellerProfileClick = {},
            onViewMoreSimilar = {},
            onViewMorePopular = {},
            onViewMoreNewest = {},
            onReportClick = {}
        )
    }
}
