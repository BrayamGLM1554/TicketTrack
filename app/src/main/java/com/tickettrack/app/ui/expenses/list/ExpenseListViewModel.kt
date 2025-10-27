package com.tickettrack.app.ui.expenses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.repository.expense.ExpenseRepository
import com.tickettrack.app.domain.model.expense.Expense
import com.tickettrack.app.domain.model.expense.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para lista de gastos
 * HU18 - Consultar y filtrar gastos
 */
class ExpenseListViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseListState())
    val state: StateFlow<ExpenseListState> = _state.asStateFlow()

    /**
     * Carga los gastos del usuario
     */
    fun loadExpenses(userId: String, userRole: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = repository.getExpensesByUser(userId)

                result.onSuccess { expenses ->
                    updateExpenses(expenses)
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar gastos"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga gastos de un viaje específico
     */
    fun loadExpensesByTrip(tripId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = repository.getExpensesByTrip(tripId)

                result.onSuccess { expenses ->
                    updateExpenses(expenses)
                    _state.value = _state.value.copy(filterByTrip = tripId)
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar gastos"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresca la lista de gastos
     */
    fun refreshExpenses(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)

            try {
                val result = repository.getExpensesByUser(userId)

                result.onSuccess { expenses ->
                    updateExpenses(expenses)
                    _state.value = _state.value.copy(isRefreshing = false)
                }.onFailure {
                    _state.value = _state.value.copy(isRefreshing = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Actualiza los gastos y aplica filtros
     */
    private fun updateExpenses(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        val count = expenses.size

        _state.value = _state.value.copy(
            expenses = expenses,
            filteredExpenses = applyFilters(expenses),
            totalAmount = total,
            expenseCount = count,
            isLoading = false
        )
    }

    /**
     * Aplica filtros a la lista de gastos
     */
    private fun applyFilters(expenses: List<Expense>): List<Expense> {
        var filtered = expenses

        // Filtrar por categoría
        _state.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filtrar por búsqueda
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.description.lowercase().contains(query) ||
                        it.category.displayName.lowercase().contains(query)
            }
        }

        return filtered.sortedByDescending { it.date }
    }

    /**
     * Actualiza el filtro de categoría
     */
    fun onCategoryFilterChanged(category: ExpenseCategory?) {
        _state.value = _state.value.copy(
            selectedCategory = category,
            filteredExpenses = applyFilters(_state.value.expenses)
        )
    }

    /**
     * Actualiza la búsqueda
     */
    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredExpenses = applyFilters(_state.value.expenses)
        )
    }

    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        _state.value = _state.value.copy(
            selectedCategory = null,
            searchQuery = "",
            filteredExpenses = _state.value.expenses
        )
    }

    /**
     * Muestra el diálogo de filtros
     */
    fun showFilterDialog() {
        _state.value = _state.value.copy(showFilterDialog = true)
    }

    /**
     * Oculta el diálogo de filtros
     */
    fun hideFilterDialog() {
        _state.value = _state.value.copy(showFilterDialog = false)
    }

    /**
     * Muestra confirmación de eliminación
     */
    fun showDeleteConfirmation(expense: Expense) {
        _state.value = _state.value.copy(expenseToDelete = expense)
    }

    /**
     * Oculta confirmación de eliminación
     */
    fun hideDeleteConfirmation() {
        _state.value = _state.value.copy(expenseToDelete = null)
    }

    /**
     * Elimina un gasto
     * HU21 - Borrado de registros
     */
    fun deleteExpense(expenseId: String, userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteExpense(expenseId)

                result.onSuccess {
                    // Recargar gastos
                    loadExpenses(userId, "")
                    _state.value = _state.value.copy(expenseToDelete = null)
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        errorMessage = error.message ?: "Error al eliminar gasto"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}