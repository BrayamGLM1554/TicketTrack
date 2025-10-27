package com.tickettrack.app.ui.expenses.budget

import com.tickettrack.app.domain.model.expense.BudgetRequest

/**
 * Estado para la pantalla de solicitudes de presupuesto
 * HU20 - Ver y gestionar solicitudes de aumento de presupuesto
 */
data class BudgetRequestState(
    // Lista de solicitudes
    val requests: List<BudgetRequest> = emptyList(),
    val pendingCount: Int = 0,

    // Estados de UI
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Solicitud expandida
    val expandedRequestId: String? = null,

    // Diálogo de revisión
    val requestToReview: BudgetRequest? = null,
    val reviewNote: String = "",
    val reviewNoteError: String? = null,
    val isApproving: Boolean = false
)