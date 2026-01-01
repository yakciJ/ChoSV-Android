package com.chosv.chosv_android.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.convertBaseUrl
import com.chosv.chosv_android.data.model.CategoryTree
import com.chosv.chosv_android.navigation.AppNavHost
import com.chosv.chosv_android.ui.components.BottomNavigationBar
import com.chosv.chosv_android.ui.components.MainTopAppBar
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun MainScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val application = LocalContext.current.applicationContext as ChoSVApplication
    val categoryRepository = application.container.categoryRepository

    var categories by remember { mutableStateOf<List<CategoryTree>>(emptyList()) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    val expandedCategories = remember { mutableStateMapOf<Int, Boolean>() }

    // Load categories khi mở drawer
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen && categories.isEmpty()) {
            try {
                categories = categoryRepository.getCategoryTree()
            } catch (_: Exception) {
                // Ignore error
            } finally {
                isLoadingCategories = false
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Header với nút đóng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Đóng menu"
                        )
                    }

                    Text(
                        text = "Danh mục sản phẩm",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                if (isLoadingCategories) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn {
                        items(categories) { category ->
                            CategoryDrawerItem(
                                category = category,
                                isExpanded = expandedCategories[category.categoryId] == true,
                                onExpandClick = {
                                    expandedCategories[category.categoryId] =
                                        !(expandedCategories[category.categoryId] ?: false)
                                },
                                onCategoryClick = { categoryId, categoryName ->
                                    scope.launch { drawerState.close() }
                                    val encodedName = URLEncoder.encode(categoryName, "UTF-8")
                                    navController.navigate("browsing/category/$categoryId/$encodedName")
                                }
                            )
                        }
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                MainTopAppBar(
                    onNavigationIconClick = {
                        scope.launch { drawerState.open() }
                    },
                    onSearchActionClick = { /* TODO: Navigate to Search Screen */ }
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CategoryDrawerItem(
    category: CategoryTree,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onCategoryClick: (Int, String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (category.childs.isNotEmpty()) {
                        onExpandClick()
                    } else {
                        onCategoryClick(category.categoryId, category.name)
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category image
            Image(
                painter = rememberAsyncImagePainter(model = convertBaseUrl(category.imageUrl)),
                contentDescription = category.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Category name
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Expand/Arrow icon
            if (category.childs.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Xem",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Child categories
        if (isExpanded && category.childs.isNotEmpty()) {
            category.childs.forEach { child ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(child.categoryId, child.name) }
                        .padding(start = 60.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = convertBaseUrl(child.imageUrl)),
                        contentDescription = child.name,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Xem",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
