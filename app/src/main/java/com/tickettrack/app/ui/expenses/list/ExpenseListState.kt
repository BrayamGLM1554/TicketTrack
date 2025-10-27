package com.tickettrack.app.ui.expenses.list

import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory

/**
 * Estado para la pantalla de lista de gastos
 * HU18 - Consultar Gastos
 */
data class ExpenseListState(
    // Lista de gastos
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),

    // Filtros
    val selectedCategory: ExpenseCategory? = null,
    val searchQuery: String = "",
    val filterByTrip: String? = null,

    // Estados de UI
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Estadísticas rápidas
    val totalAmount: Double = 0.0,
    val expenseCount: Int = 0,

    // Diálogos
    val showFilterDialog: Boolean = false,
    val expenseToDelete: Expense? = null
) {
    /**
     * Obtiene el total formateado
     */
    fun getFormattedTotal(): String {
        return "$${"%.2f".format(totalAmount)}"
    }

    /**
     * Verifica si hay filtros activos
     */
    fun hasActiveFilters(): Boolean {
        return selectedCategory != null || searchQuery.isNotBlank() || filterByTrip != null
    }
}