package com.tembt.android

import android.app.Application
import com.tembt.di.androidPlatformModule
import com.tembt.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TembtApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(platformModules = listOf(androidPlatformModule)) {
            androidContext(this@TembtApp)
            androidLogger()
        }
    }
}
