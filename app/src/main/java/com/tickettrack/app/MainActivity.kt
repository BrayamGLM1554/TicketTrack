package com.tickettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.ui.forgotpassword.ForgotPasswordScreen
import com.tickettrack.app.ui.login.LoginScreen
import com.tickettrack.app.ui.register.RegisterEmployeeScreen
import com.tickettrack.app.ui.register.RegisterScreen
import com.tickettrack.app.ui.register.RegisterViewModel
import com.tickettrack.app.ui.splash.SplashScreen
import com.tickettrack.app.ui.theme.TicketTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicketTrackTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("splash") }

    // ViewModel compartido para las pantallas de registro
    val registerViewModel: RegisterViewModel = viewModel()

    when (currentScreen) {
        "splash" -> SplashScreen(
            onSplashFinished = {
                currentScreen = "login"
            }
        )

        "login" -> LoginScreen(
            onNavigateToRegister = {
                currentScreen = "register"
            },
            onNavigateToForgotPassword = {
                currentScreen = "forgotPassword"
            }
        )

        "forgotPassword" -> ForgotPasswordScreen(
            onNavigateBack = {
                currentScreen = "login"
            },
            onNavigateToLogin = {
                currentScreen = "login"
            }
        )

        "register" -> RegisterScreen(
            onNavigateToEmployeeScreen = {
                currentScreen = "registerEmployee"
            },
            onNavigateToLogin = {
                currentScreen = "login"
            },
            viewModel = registerViewModel
        )

        "registerEmployee" -> RegisterEmployeeScreen(
            onNavigateToLogin = {
                currentScreen = "login"
            },
            onRegistrationSuccess = {
                // Aquí puedes decidir a dónde ir después del registro exitoso
                // Por ahora vamos al login
                currentScreen = "login"
            },
            onNavigateBack = {
                // Regresa a la pantalla anterior (RegisterScreen)
                currentScreen = "register"
            },
            viewModel = registerViewModel
        )
    }
}