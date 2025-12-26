package com.chosv.chosv_android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    onNavigationIconClick: () -> Unit,
    onSearchActionClick: () -> Unit
) {
    TopAppBar(
        title = { Text("ChoSV") },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Drawer")
            }
        },
        actions = {
            IconButton(onClick = onSearchActionClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}

// --- KHỐI CODE PREVIEW ---
@Preview(showBackground = true)
@Composable
fun MainTopAppBarPreview() {
    ChoSVAndroidTheme {
        MainTopAppBar(
            onNavigationIconClick = {},
            onSearchActionClick = {}
        )
    }
}