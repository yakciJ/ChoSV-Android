package com.chosv.chosv_android.data.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.chosv.chosv_android.R

sealed class Screen(val route: String, val icon: ImageVector?, @StringRes val label: Int) {
    data object Login : Screen("login", null, R.string.login)
    data object SignUp : Screen("signUp", null, R.string.login)
    data object Home : Screen("home", Icons.Default.Home, R.string.home)

}