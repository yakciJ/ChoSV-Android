package com.chosv.chosv_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chosv.chosv_android.ui.theme.ChoSVAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as ChoSVApplication).container
        setContent {
            ChoSVAndroidTheme {
                App()
            }
        }
    }
}