package com.tickettrack.app

import android.app.Application
import com.tickettrack.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TicketTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar Koin
        startKoin {
            androidLogger()
            androidContext(this@TicketTrackApplication)
            modules(appModule)
        }
    }
}