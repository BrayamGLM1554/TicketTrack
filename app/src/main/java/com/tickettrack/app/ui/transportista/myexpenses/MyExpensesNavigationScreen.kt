package com.tickettrack.app.ui.transportista.myexpenses

import androidx.compose.runtime.*

/**
 * Pantalla de navegación interna para el módulo de Mis Gastos.
 *
 * Por ahora solo muestra la lista, pero está preparada para:
 * - Navegación a detalle de gasto (si se implementa)
 * - Navegación a registro de gasto
 */
@Composable
fun MyExpensesNavigationScreen() {
    // Estado de navegación
    var currentScreen by remember { mutableStateOf("list") }

    when (currentScreen) {
        "list" -> {
            MyExpensesScreen(
                onNavigateToRegisterExpense = {
                    // TODO: Navegar a ExpenseRegisterScreen
                    // Por ahora no hace nada, el FAB está preparado
                    currentScreen = "register"
                }
            )
        }

        "register" -> {
            // TODO: Implementar cuando tengamos ExpenseRegisterScreen
            // Por ahora volver a la lista
            currentScreen = "list"
        }
    }
}