package com.tickettrack.app.ui.transportista

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.ui.transportista.budgetrequest.BudgetRequestScreen
import com.tickettrack.app.ui.transportista.myexpenses.MyExpensesNavigationScreen
import com.tickettrack.app.ui.transportista.mytrips.MyTripsNavigationScreen
import com.tickettrack.app.ui.transportista.profile.TransportistaProfileScreen

/**
 * Pantalla de navegación principal del módulo TRANSPORTISTA (USER).
 *
 * Gestiona la navegación entre todas las secciones del transportista:
 * - Dashboard
 * - Mis Viajes (con detalle)
 * - Mis Gastos
 * - Solicitar Presupuesto
 * - Perfil
 *
 * Esta pantalla actúa como "hub central" del transportista.
 */
@Composable
fun TransportistaMainNavigationScreen(
    selectedTab: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Estado de navegación interno
    var currentScreen by remember { mutableStateOf("main") }
    var selectedTripId by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        "main" -> {
            // Mostrar la pantalla según el tab seleccionado
            when (selectedTab) {
                "Inicio" -> {
                    TransportistaDashboardScreen()
                }

                "Viajes" -> {
                    MyTripsNavigationScreen()
                }

                "Gastos" -> {
                    MyExpensesNavigationScreen()
                }

                "Transportista" -> {
                    // Para el USER, el tab "Transportista" muestra su perfil
                    TransportistaProfileScreen(
                        onLogout = onLogout
                    )
                }
            }
        }

        "budgetRequest" -> {
            BudgetRequestScreen(
                tripId = selectedTripId,
                onNavigateBack = {
                    currentScreen = "main"
                    selectedTripId = null
                },
                onRequestSubmitted = {
                    currentScreen = "main"
                    selectedTripId = null
                }
            )
        }
    }
}