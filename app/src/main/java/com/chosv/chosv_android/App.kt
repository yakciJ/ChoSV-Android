package com.chosv.chosv_android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.navigation.AppNavHost
import com.chosv.chosv_android.preferences.TokenPreferences

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Dùng giá trị khởi tạo là "" để đại diện cho trạng thái "đang tải token"
    val accessToken by TokenPreferences(context).accessTokenFlow.collectAsState(initial = "")

    // Nếu accessToken vẫn là giá trị khởi tạo, có nghĩa là ta đang chờ kết quả từ DataStore.
    // Trong lúc này, hiển thị một vòng xoay loading là tốt nhất.
    if (accessToken == "") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Khi đã có kết quả (accessToken là null hoặc một chuỗi có giá trị),
        // ta sẽ quyết định màn hình bắt đầu và hiển thị NavHost.
        val startDestination = if (accessToken != null) {
            // Nếu token không null -> đã đăng nhập -> vào Home
            Screen.Home.route
        } else {
            // Nếu token là null -> chưa đăng nhập -> vào Login
            Screen.Login.route
        }
        AppNavHost(navController = navController, startDestination = startDestination)
    }
}