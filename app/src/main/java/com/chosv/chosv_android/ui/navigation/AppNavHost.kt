package com.chosv.chosv_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.ui.screen.auth.LoginScreen


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignupClick = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true } // Xóa màn login khỏi backstack
                    }
                }
            )
        }
    }
}