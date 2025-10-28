package com.tickettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.data.remote.TripRetrofitClient
import com.tickettrack.app.ui.forgotpassword.ForgotPasswordScreen
import com.tickettrack.app.ui.login.LoginScreen
import com.tickettrack.app.ui.main.MainScreen
import com.tickettrack.app.ui.register.RegisterEmployeeScreen
import com.tickettrack.app.ui.register.RegisterScreen
import com.tickettrack.app.ui.register.RegisterViewModel
import com.tickettrack.app.ui.splash.SplashScreen
import com.tickettrack.app.ui.theme.TicketTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ CRÍTICO: Inicializar TripRetrofitClient ANTES de setContent
        initializeTripModule()

        setContent {
            TicketTrackTheme {
                AppNavigation()
            }
        }
    }

    /**
     * Inicializa el módulo de trips con TokenManager.
     * Debe llamarse ANTES de cualquier uso de TripRetrofitClient.
     */
    private fun initializeTripModule() {
        val tokenManager = TokenManager(this)
        TripRetrofitClient.initialize(tokenManager)
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
            onNavigateToRegister = { currentScreen = "register" },
            onNavigateToForgotPassword = { currentScreen = "forgotPassword" },
            onLoginSuccess = { currentScreen = "main" }
        )

        "main" -> MainScreen(
            onLogout = { currentScreen = "login" }
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
                currentScreen = "login"
            },
            onNavigateBack = {
                currentScreen = "register"
            },
            viewModel = registerViewModel
        )
    }
}