package com.chosv.chosv_android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.navigation.AppNavHost
import com.chosv.chosv_android.ui.screen.main.MainScreen

/**
 * Trạng thái xác thực của ứng dụng
 */
sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as ChoSVApplication
    val userRepository = application.container.userRepository

    var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }

    // Kiểm tra đăng nhập khi mở app
    LaunchedEffect(Unit) {
        val user = userRepository.getCurrentUser()
        authState = if (user != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    when (authState) {
        is AuthState.Loading -> {
            // Màn hình loading khi đang kiểm tra auth
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            // Đã đăng nhập -> vào MainScreen
            MainScreen(navController = navController)
        }
        is AuthState.Unauthenticated -> {
            // Chưa đăng nhập -> vào AppNavHost với start là Login
            AppNavHost(navController = navController, startDestination = Screen.Login.route)
        }
    }
}