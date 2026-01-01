package com.chosv.chosv_android.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import com.chosv.chosv_android.R

sealed class Screen(val route: String, val icon: ImageVector?, @StringRes val label: Int) {
    data object Login : Screen("login", null, R.string.login)
    data object Signup : Screen("signup", null, R.string.signup)
    data object Home : Screen("home", Icons.Filled.Home, R.string.home)
    data object Management : Screen("management",
        Icons.AutoMirrored.Filled.Article, R.string.management)
    data object CreatePost : Screen("create_post",
        Icons.Filled.Add, R.string.create_post)
    data object Messages : Screen("messages", Icons.AutoMirrored.Filled.Chat,R.string.msg)
    data object Profile : Screen("profile",Icons.Filled.Person, R.string.profile)
    data object Favorites : Screen("favorites", null, R.string.favorites)
    data object Product : Screen("products/{id}", null, R.string.product_detail)
    data object EditProduct : Screen("edit_product/{productId}", null, R.string.edit_product)

    // Browsing screens
    data object BrowsingNewest : Screen("browsing/newest", null, R.string.browsing_newest)
    data object BrowsingPopular : Screen("browsing/popular", null, R.string.browsing_popular)
    data object BrowsingCategory : Screen("browsing/category/{categoryId}/{categoryName}", null, R.string.browsing_category)
}

val bottomNavItems = listOf(
    Screen.Home, Screen.Management, Screen.CreatePost, Screen.Messages, Screen.Profile
)