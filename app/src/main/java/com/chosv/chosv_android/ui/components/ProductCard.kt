package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chosv.chosv_android.data.model.Category
import com.chosv.chosv_android.data.model.Product
import com.chosv.chosv_android.formatCurrency
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme
import com.chosv.chosv_android.convertBaseUrl
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext



@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onCardClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Box chứa ảnh sản phẩm và icon yêu thích
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current).data(convertBaseUrl(product.firstImageUrl))
                        .crossfade(true).build(),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f), // Giữ cho ảnh luôn vuông
                    contentScale = ContentScale.Crop
                )

                // Nền tròn xám trong suốt cho icon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (product.isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Cột chứa thông tin chữ
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tên sản phẩm
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Giá sản phẩm
                Text(
                    // --- THAY ĐỔI Ở ĐÂY ---
                    text = formatCurrency(product.price),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Màu primary cho giá
                )

                // Hàng chứa thông tin người bán
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context = LocalContext.current).data(convertBaseUrl(product.sellerAvatar))
                            .crossfade(true).build(),
                        contentDescription = "Seller Avatar",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = product.sellerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- KHỐI CODE PREVIEW (giữ nguyên) ---
@Preview(showBackground = true, widthDp = 200)
@Composable
fun ProductCardPreview() {
    // Tạo một sản phẩm mẫu để hiển thị trong preview
    val sampleProduct = Product(
        productId = 1,
        productName = "Macbook Pro M3 16GB 512GB siêu mới, siêu lướt",
        price = 35000000.0, // <-- SỬA LẠI THÀNH DOUBLE
        status = "Approved",
        firstImageUrl = "", // Để trống, preview sẽ hiển thị placeholder
        createdDate = "2025-12-25T10:00:00Z",
        sellerName = "jickay",
        sellerFullName = "J1ckay",
        sellerAvatar = "", // Để trống, preview sẽ hiển thị placeholder
        isFavorited = true,
        favoriteCount = 99,
        categories = listOf(Category(1, "", "Đồ điện tử"))
    )

    ChoSVAndroidTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ProductCard(
                product = sampleProduct,
                onCardClick = {},
                onFavoriteClick = {}
            )
        }
    }
}