package com.tickettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.tickettrack.app.ui.login.LoginScreen
import com.tickettrack.app.ui.splash.SplashScreen
import com.tickettrack.app.ui.theme.TicketTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketTrackTheme {
                var currentScreen by remember { mutableStateOf("splash") }

                when (currentScreen) {
                    "splash" -> SplashScreen(onSplashFinished = {
                        currentScreen = "login"
                    })
                    "login" -> LoginScreen()
                }
            }
        }
    }
}
