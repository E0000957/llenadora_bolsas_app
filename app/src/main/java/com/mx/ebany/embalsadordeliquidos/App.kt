package com.mx.ebany.embalsadordeliquidos

import android.app.Application
import com.mx.ebany.embalsadordeliquidos.core.di.ViewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(ViewModelsModule)
        }
    }
}