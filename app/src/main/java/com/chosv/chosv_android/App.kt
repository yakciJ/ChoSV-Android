package com.chosv.chosv_android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.ui.navigation.AppNavHost

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    AppNavHost(navController = navController)
//
//    val isMainScreen = currentRoute in listOf(
//        Screen.Home.route,
//        Screen.Explore.route,
//        Screen.Cart.route,
//        Screen.Favorites.route,
//        Screen.Profile.route
//    )
//
//    if (isMainScreen) {
//        MainScreen(navController = navController)
//    } else {
//        AppNavHost(navController = navController)
//    }
}