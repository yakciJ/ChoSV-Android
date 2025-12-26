package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme
import kotlinx.coroutines.launch

@Composable
fun MainLayout(
    navController: NavController,
    content: @Composable (modifier: Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // TODO: Hiển thị danh sách Category ở đây
                Text("Category 1", modifier = Modifier.padding(16.dp))
                Text("Category 2", modifier = Modifier.padding(16.dp))
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
            // Nội dung của màn hình (ví dụ: HomeScreen) sẽ được đặt ở đây
            content(Modifier.padding(innerPadding))
        }
    }
}

// --- KHỐI CODE PREVIEW CHO MAINLAYOUT ---
@Preview(showSystemUi = true) // showSystemUi = true để hiển thị cả status bar, giống màn hình thật
@Composable
fun MainLayoutPreview() {
    ChoSVAndroidTheme {
        MainLayout(
            navController = rememberNavController(),
            content = { modifier ->
                // Đặt một nội dung giả lập vào giữa để xem bố cục
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nội dung màn hình ở đây")
                }
            }
        )
    }
}