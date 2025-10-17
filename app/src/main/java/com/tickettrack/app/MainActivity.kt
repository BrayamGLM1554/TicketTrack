package com.tickettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tickettrack.app.ui.main.MainScreen
import com.tickettrack.app.ui.splash.SplashScreen
import com.tickettrack.app.ui.theme.TicketTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketTrackTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    MainScreen()
                }
            }
        }
    }
}
