package com.chosv.chosv_android.ui.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.chosv.chosv_android.navigation.AppNavHost
import com.chosv.chosv_android.ui.components.BottomNavigationBar
import com.chosv.chosv_android.ui.components.MainTopAppBar
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
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
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
