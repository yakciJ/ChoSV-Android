package com.chosv.chosv_android

import android.app.Application
import com.chosv.chosv_android.data.AppContainer
import com.chosv.chosv_android.data.DefaultAppContainer

class ChoSVApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}