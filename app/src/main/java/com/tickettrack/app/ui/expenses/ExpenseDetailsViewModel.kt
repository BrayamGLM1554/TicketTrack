package com.tickettrack.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.domain.model.Expense
import com.tickettrack.app.domain.repository.IExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpenseDetailsUiState(
    val isLoading: Boolean = false,
    val expense: Expense? = null,
    val error: String? = null
)

class ExpenseDetailsViewModel(
    private val expensesRepository: IExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseDetailsUiState())
    val uiState: StateFlow<ExpenseDetailsUiState> = _uiState.asStateFlow()

    fun loadExpenseDetails(token: String, expenseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = expensesRepository.getExpenseById(token, expenseId)

            result.fold(
                onSuccess = { expense ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expense = expense
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar detalles del gasto"
                    )
                }
            )
        }
    }
}