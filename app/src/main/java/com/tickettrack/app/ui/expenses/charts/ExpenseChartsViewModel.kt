package com.tickettrack.app.ui.expenses.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.repository.expense.ExpenseRepository
import com.tickettrack.app.domain.model.expense.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gráficas de gastos
 * HU19 - Visualizar gastos en gráfica cambiante por semana, mes y año
 */
class ExpenseChartsViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseChartsState())
    val state: StateFlow<ExpenseChartsState> = _state.asStateFlow()

    /**
     * Carga las estadísticas de gastos
     */
    fun loadStats(userId: String, period: TimePeriod = TimePeriod.MONTH) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedPeriod = period
            )

            try {
                val result = repository.getExpenseStats(userId, period)

                result.onSuccess { stats ->
                    _state.value = _state.value.copy(
                        stats = stats,
                        isLoading = false
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar estadísticas"
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
     * Cambia el período de tiempo
     */
    fun changePeriod(period: TimePeriod, userId: String) {
        loadStats(userId, period)
        _state.value = _state.value.copy(showPeriodDialog = false)
    }

    /**
     * Muestra el diálogo de período
     */
    fun showPeriodDialog() {
        _state.value = _state.value.copy(showPeriodDialog = true)
    }

    /**
     * Oculta el diálogo de período
     */
    fun hidePeriodDialog() {
        _state.value = _state.value.copy(showPeriodDialog = false)
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}