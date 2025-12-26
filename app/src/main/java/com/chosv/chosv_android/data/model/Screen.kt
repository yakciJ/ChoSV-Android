package com.chosv.chosv_android.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val unselectedIcon: ImageVector? = null,
                    val selectedIcon: ImageVector? = null) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Int) = "product/$productId"
    }


    object Home : Screen("home", "Trang chủ", Icons.Outlined.Home, Icons.Filled.Home)
    object Management : Screen("management", "Quản lý", Icons.Outlined.Article, Icons.Filled.Article)
    object CreatePost : Screen("create_post", "Đăng tin", Icons.Outlined.AddCircleOutline, Icons.Outlined.AddCircleOutline)
    object Messages : Screen("messages", "Nhắn tin", Icons.Outlined.Chat, Icons.Filled.Chat)
    object Profile : Screen("profile", "Cá nhân", Icons.Outlined.Person, Icons.Filled.Person)
}