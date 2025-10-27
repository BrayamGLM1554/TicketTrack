package com.tickettrack.app.ui.expenses

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tickettrack.app.data.local.TokenManager
import com.tickettrack.app.ui.expenses.list.ExpenseListScreen
import com.tickettrack.app.ui.expenses.register.ExpenseRegisterScreen
import com.tickettrack.app.ui.expenses.charts.ExpenseChartsScreen
import com.tickettrack.app.ui.expenses.budget.BudgetRequestListScreen

/**
 * Pantalla principal de navegación para el módulo de gastos
 * Se integra con el BottomNavBar desde MainScreen
 */
@Composable
fun ExpenseMainNavigationScreen() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Obtener datos del usuario
    val userId = tokenManager.getUserEmail() ?: ""
    val userName = tokenManager.getUserName() ?: ""
    val userRole = tokenManager.getUserRole() ?: ""
    val companyEmail = tokenManager.getCompanyEmail() ?: ""

    // Estado de navegación interna del módulo
    var currentExpenseScreen by remember { mutableStateOf("list") }
    var selectedTripId by remember { mutableStateOf<String?>(null) }

    when (currentExpenseScreen) {
        "list" -> {
            ExpenseListScreen(
                userId = userId,
                userRole = userRole,
                tripId = selectedTripId,
                onNavigateToRegister = {
                    // Para registrar gasto, necesitamos un tripId
                    // Aquí podrías mostrar un diálogo para seleccionar viaje
                    // o navegar a la lista de viajes
                    // Por ahora, solo si tenemos un tripId
                    if (selectedTripId != null) {
                        currentExpenseScreen = "register"
                    }
                },
                onNavigateToCharts = {
                    currentExpenseScreen = "charts"
                },
                onNavigateToBudgetRequests = {
                    currentExpenseScreen = "budget"
                }
            )
        }

        "register" -> {
            selectedTripId?.let { tripId ->
                ExpenseRegisterScreen(
                    tripId = tripId,
                    userId = userId,
                    userName = userName,
                    onNavigateBack = {
                        currentExpenseScreen = "list"
                    },
                    onExpenseRegistered = {
                        currentExpenseScreen = "list"
                    }
                )
            }
        }

        "charts" -> {
            ExpenseChartsScreen(
                userId = userId,
                onNavigateBack = {
                    currentExpenseScreen = "list"
                }
            )
        }

        "budget" -> {
            BudgetRequestListScreen(
                userEmail = userId,
                userName = userName,
                userRole = userRole,
                companyEmail = companyEmail,
                onNavigateBack = {
                    currentExpenseScreen = "list"
                }
            )
        }
    }
}