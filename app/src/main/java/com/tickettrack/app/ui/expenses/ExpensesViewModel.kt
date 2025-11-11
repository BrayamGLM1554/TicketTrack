package com.tickettrack.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.*
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository
import com.tickettrack.app.domain.repository.IExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val expensesByCategory: List<ExpensesByCategory> = emptyList(),
    val totalExpenses: Double = 0.0,
    val error: String? = null,

    // Budget Requests
    val isLoadingBudgetRequests: Boolean = false,
    val pendingBudgetRequests: List<BudgetRequest> = emptyList(),
    val budgetRequestsError: String? = null
)

class ExpensesViewModel(
    private val expensesRepository: IExpensesRepository,
    private val budgetRequestsRepository: IBudgetRequestsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    fun loadData(token: String) {
        loadExpenses(token)
        loadPendingBudgetRequests(token)
    }

    private fun loadExpenses(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = expensesRepository.getExpenses(token, limit = 1000)

            result.fold(
                onSuccess = { response ->
                    val expenses = response.expenses
                    val byCategory = calculateExpensesByCategory(expenses)
                    val total = expenses.sumOf { it.amount }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expenses = expenses,
                        expensesByCategory = byCategory,
                        totalExpenses = total
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar gastos"
                    )
                }
            )
        }
    }

    private fun loadPendingBudgetRequests(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingBudgetRequests = true,
                budgetRequestsError = null
            )

            val result = budgetRequestsRepository.getPendingBudgetRequests(token)

            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingBudgetRequests = false,
                        pendingBudgetRequests = response.data
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingBudgetRequests = false,
                        budgetRequestsError = exception.message ?: "Error al cargar solicitudes"
                    )
                }
            )
        }
    }

    private fun calculateExpensesByCategory(expenses: List<Expense>): List<ExpensesByCategory> {
        if (expenses.isEmpty()) return emptyList()

        val total = expenses.sumOf { it.amount }

        return expenses
            .groupBy { it.category }
            .map { (category, expensesList) ->
                val categoryTotal = expensesList.sumOf { it.amount }
                ExpensesByCategory(
                    category = category,
                    totalAmount = categoryTotal,
                    count = expensesList.size,
                    percentage = ((categoryTotal / total) * 100).toFloat()
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    fun refreshData(token: String) {
        loadData(token)
    }
}