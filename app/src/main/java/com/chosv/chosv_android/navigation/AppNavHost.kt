package com.chosv.chosv_android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.ui.components.MainLayout
import com.chosv.chosv_android.ui.screen.auth.LoginScreen
import com.chosv.chosv_android.ui.screen.home.HomeScreen
import com.chosv.chosv_android.ui.screen.management.CreatePostScreen
import com.chosv.chosv_android.ui.screen.management.ManagementScreen
import com.chosv.chosv_android.ui.screen.messages.MessagesScreen
import com.chosv.chosv_android.ui.screen.profile.ProfileScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String) {
    NavHost(navController, startDestination = startDestination) {

        // --- Màn hình không có layout chính ---
        composable(Screen.Login.route) {
            LoginScreen(
                onSignupClick = { /* ... */ },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Các màn hình CÓ layout chính (dùng MainLayout) ---
        composable(Screen.Home.route) {
            MainLayout(navController = navController) { modifier ->
                HomeScreen(
                    modifier = modifier,
                    // --- SỬA Ở ĐÂY ---
                    // Truyền vào hành động điều hướng khi một sản phẩm được click
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }
        }

        composable(Screen.Management.route) {
            MainLayout(navController = navController) { modifier ->
                ManagementScreen(modifier = modifier)
            }
        }

        composable(Screen.CreatePost.route) {
            MainLayout(navController = navController) { modifier ->
                CreatePostScreen(modifier = modifier)
            }
        }

        composable(Screen.Messages.route) {
            MainLayout(navController = navController) { modifier ->
                MessagesScreen(modifier = modifier)
            }
        }

        composable(Screen.Profile.route) {
            MainLayout(navController = navController) { modifier ->
                ProfileScreen(modifier = modifier)
            }
        }

        // --- THÊM MÀN HÌNH CHI TIẾT SẢN PHẨM ---
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable

            // TODO: Tạo ProductDetailScreen(productId = productId) thật
            // Dưới đây chỉ là một màn hình giả lập
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Màn hình chi tiết của sản phẩm ID: $productId")
            }
        }
    }
}