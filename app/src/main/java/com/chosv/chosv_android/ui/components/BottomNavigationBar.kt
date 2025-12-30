package com.chosv.chosv_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chosv.chosv_android.data.model.Screen
import com.chosv.chosv_android.data.model.bottomNavItems
import com.chosv.chosv_android.preferences.TokenPreferences
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    tokenPreferences: TokenPreferences = TokenPreferences(LocalContext.current)
) {
    val accessToken by tokenPreferences.accessTokenFlow.collectAsState(initial = null)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    if (screen.route == Screen.CreatePost.route) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = screen.icon ?: Icons.Filled.QuestionMark,
                                contentDescription = stringResource(id = screen.label),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = screen.icon ?: Icons.Filled.QuestionMark,
                            contentDescription = stringResource(id = screen.label)
                        )
                    }
                },
                label = {
                    if (screen.route != Screen.CreatePost.route) {
                        Text(text = stringResource(id = screen.label))
                    }
                },
                selected = currentRoute == screen.route,
                onClick = {
                    if (screen.route == Screen.Profile.route && accessToken == null) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up để tránh stack quá sâu
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

// --- KHỐI CODE PREVIEW ---
@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    ChoSVAndroidTheme {
        BottomNavigationBar(navController = rememberNavController())
    }
}
