package com.tickettrack.app.ui.expenses.charts

import com.tickettrack.app.domain.model.expense.ExpenseStats
import com.tickettrack.app.domain.model.expense.TimePeriod

/**
 * Estado para la pantalla de gráficas de gastos
 * HU19 - Visualizar gastos en gráfica
 */
data class ExpenseChartsState(
    // Estadísticas
    val stats: ExpenseStats? = null,

    // Período seleccionado
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,

    // Estados de UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Diálogo de período
    val showPeriodDialog: Boolean = false
)