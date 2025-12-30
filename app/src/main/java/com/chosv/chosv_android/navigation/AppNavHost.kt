package com.chosv.chosv_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.ui.screen.auth.LoginScreen
import com.chosv.chosv_android.ui.screen.favorite.FavoriteScreen
import com.chosv.chosv_android.ui.screen.home.HomeScreen
import com.chosv.chosv_android.ui.screen.management.CreatePostScreen
import com.chosv.chosv_android.ui.screen.management.ManagementScreen
import com.chosv.chosv_android.ui.screen.messages.MessagesScreen
import com.chosv.chosv_android.ui.screen.product.ProductScreen
import com.chosv.chosv_android.ui.screen.profile.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(navController, startDestination = startDestination, modifier = modifier) {

        // --- Màn hình Login ---
        composable(Screen.Login.route) {
            LoginScreen(
                onSignupClick = {
                    navController.navigate(Screen.Signup.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Các màn hình chính (bottom nav) ---
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Management.route) {
            ManagementScreen(navController = navController)
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(navController = navController)
        }

        composable(Screen.Messages.route) {
            MessagesScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // --- Màn hình sản phẩm yêu thích ---
        composable(Screen.Favorites.route) {
            FavoriteScreen(navController = navController)
        }

        // --- Màn hình chi tiết sản phẩm ---
        composable(
            route = Screen.Product.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("id") ?: return@composable
            ProductScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
    }
}