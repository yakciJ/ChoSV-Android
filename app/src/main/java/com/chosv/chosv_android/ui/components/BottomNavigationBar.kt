package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Management,
        Screen.CreatePost,
        Screen.Messages,
        Screen.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            if (screen.route == Screen.CreatePost.route) {
                // --- Logic cho nút Đăng tin (giữ nguyên) ---
                NavigationBarItem(
                    selected = false,
                    onClick = { if (currentRoute != screen.route) navController.navigate(screen.route) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.unselectedIcon!!,
                                contentDescription = screen.title,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            } else {
                // --- SỬA ĐỔI LOGIC CHO CÁC NÚT BÌNH THƯỜNG ---
                val isSelected = currentRoute == screen.route
                NavigationBarItem(
                    icon = {
                        // Chọn icon dựa trên trạng thái isSelected
                        val icon = if (isSelected) screen.selectedIcon!! else screen.unselectedIcon!!
                        Icon(imageVector = icon, contentDescription = screen.title)
                    },
                    label = { Text(screen.title!!, softWrap = false) },
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    // --- THÊM THAM SỐ `colors` ĐỂ TÙY CHỈNH MÀU SẮC ---
                    colors = NavigationBarItemDefaults.colors(
                        // Màu của icon và chữ khi được chọn
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,

                        // Màu của icon và chữ khi không được chọn
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        // Màu của nền indicator (cái hình viên thuốc) phía sau icon
                        indicatorColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

// --- KHỐI CODE PREVIEW (giữ nguyên) ---
@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    ChoSVAndroidTheme {
        BottomNavigationBar(navController = rememberNavController())
    }
}
