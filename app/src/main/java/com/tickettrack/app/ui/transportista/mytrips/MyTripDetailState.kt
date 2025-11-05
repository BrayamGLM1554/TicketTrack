package com.tickettrack.app.ui.transportista.mytrips

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus
import com.tickettrack.app.domain.model.expense.Expense

/**
 * Estado del detalle de un viaje para el transportista.
 *
 * Muestra:
 * - Información completa del viaje
 * - Lista de gastos asociados
 * - Historial de cambios de estado
 * - Historial de aumentos de presupuesto
 * - Opciones para cambiar estado (iniciar/finalizar)
 * - Opción para solicitar aumento de presupuesto
 *
 * HU relevantes: HU11 (Consultar Detalles), HU13 (Aumentar Presupuesto)
 */
data class MyTripDetailState(
    // Datos del usuario
    val currentUserId: String = "",
    val currentUserName: String = "",

    // Datos del viaje
    val trip: Trip? = null,
    val tripId: String = "",

    // Gastos del viaje
    val expenses: List<Expense> = emptyList(),
    val totalExpensesCalculated: Double = 0.0,

    // Diálogos
    val showStartTripDialog: Boolean = false,
    val showCompleteTripDialog: Boolean = false,
    val showBudgetRequestDialog: Boolean = false,

    // Estados de acciones
    val isChangingStatus: Boolean = false,
    val statusChangeSuccess: Boolean = false,

    // Estados de UI
    val isLoadingTrip: Boolean = false,
    val isLoadingExpenses: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    // Tab seleccionada
    val selectedTab: Int = 0 // 0: Detalles, 1: Gastos, 2: Historial
) {
    /**
     * Verifica si el usuario puede iniciar el viaje.
     */
    fun canStartTrip(): Boolean {
        return trip?.status == TripStatus.PENDING
    }

    /**
     * Verifica si el usuario puede completar el viaje.
     */
    fun canCompleteTrip(): Boolean {
        return trip?.status == TripStatus.IN_PROGRESS
    }

    /**
     * Verifica si el usuario puede solicitar aumento de presupuesto.
     */
    fun canRequestBudgetIncrease(): Boolean {
        return trip?.status == TripStatus.IN_PROGRESS || trip?.status == TripStatus.PENDING
    }

    /**
     * Calcula el porcentaje de uso del presupuesto.
     */
    fun getBudgetUsagePercentage(): Float {
        val budget = trip?.budget?.current ?: 0.0
        val expenses = totalExpensesCalculated
        return if (budget > 0) {
            ((expenses / budget) * 100).toFloat()
        } else 0f
    }

    /**
     * Verifica si el presupuesto está en riesgo (>= 90%).
     */
    fun isBudgetAtRisk(): Boolean {
        return getBudgetUsagePercentage() >= 90f
    }
}