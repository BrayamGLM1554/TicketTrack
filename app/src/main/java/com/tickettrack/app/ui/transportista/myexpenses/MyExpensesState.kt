package com.tickettrack.app.ui.transportista.myexpenses

import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory

/**
 * Estado de la lista de gastos del transportista.
 *
 * Muestra solo los gastos creados por el transportista (createdBy = userEmail).
 * Permite filtrar por categoría, viaje y buscar por texto.
 *
 * HU relevantes: HU16 (Registrar Gasto), HU18 (Consultar Gastos)
 */
data class MyExpensesState(
    // Datos del usuario
    val currentUserId: String = "",
    val currentUserName: String = "",

    // Lista de gastos
    val expenses: List<Expense> = emptyList(),

    // Filtros
    val selectedCategoryFilter: ExpenseCategory? = null,
    val selectedTripFilter: String? = null,
    val searchQuery: String = "",
    val showFilters: Boolean = false,

    // Estadísticas rápidas
    val totalExpenses: Double = 0.0,
    val expenseCount: Int = 0,
    val expensesByCategory: Map<ExpenseCategory, Double> = emptyMap(),

    // Viajes disponibles para filtrar
    val availableTrips: List<TripFilterOption> = emptyList(),

    // Estados de UI
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Obtiene los gastos a mostrar después de aplicar filtros.
     */
    fun getDisplayExpenses(): List<Expense> {
        var result = expenses

        // Filtrar por categoría
        selectedCategoryFilter?.let { category ->
            result = result.filter { it.category == category }
        }

        // Filtrar por viaje
        selectedTripFilter?.let { tripId ->
            result = result.filter { it.tripId == tripId }
        }

        // Filtrar por búsqueda
        if (searchQuery.isNotBlank()) {
            result = result.filter { expense ->
                expense.description.contains(searchQuery, ignoreCase = true) ||
                        expense.category.displayName.contains(searchQuery, ignoreCase = true) ||
                        expense.tripId.contains(searchQuery, ignoreCase = true)
            }
        }

        // Ordenar por fecha (más reciente primero)
        return result.sortedByDescending { it.createdAt }
    }

    /**
     * Indica si hay filtros activos.
     */
    fun hasActiveFilters(): Boolean {
        return selectedCategoryFilter != null ||
                selectedTripFilter != null ||
                searchQuery.isNotBlank()
    }

    /**
     * Calcula el total de los gastos filtrados.
     */
    fun getFilteredTotal(): Double {
        return getDisplayExpenses().sumOf { it.amount }
    }
}

/**
 * Opción de filtro por viaje.
 */
data class TripFilterOption(
    val tripId: String,
    val tripName: String,
    val expenseCount: Int
)