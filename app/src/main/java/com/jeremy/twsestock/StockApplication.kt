package com.jeremy.twsestock

import android.app.Application
import com.jeremy.twsestock.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@StockApplication)
            modules(appModule)
        }
    }
}
