package com.tickettrack.app.ui.main.trips.detail

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.Urgency
import com.tickettrack.app.ui.main.trips.create.DriverResponse

/**
 * Estado de la pantalla de detalle de viaje.
 *
 * Maneja:
 * - Información del viaje
 * - Datos del transportista
 * - Estado de secciones expandibles
 * - Diálogo de aumento de presupuesto
 */
data class TripDetailState(
    // Viaje
    val trip: Trip? = null,
    val isLoadingTrip: Boolean = false,

    // Transportista asignado
    val driver: DriverResponse? = null,
    val isLoadingDriver: Boolean = false,

    // Secciones expandibles
    val isBudgetHistoryExpanded: Boolean = false,
    val isStatusHistoryExpanded: Boolean = false,

    // Diálogo de aumentar presupuesto
    val showIncreaseBudgetDialog: Boolean = false,
    val newBudgetAmount: String = "",
    val newBudgetAmountError: String? = null,
    val increaseReason: String = "",
    val increaseReasonError: String? = null,
    val selectedUrgency: Urgency = Urgency.MEDIUM,
    val isIncreasingBudget: Boolean = false,

    // Cambio de estado
    val isChangingStatus: Boolean = false,
    val statusChangeSuccess: Boolean = false,

    // Error general
    val errorMessage: String? = null,

    // Usuario actual
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserRole: String = ""
) {
    /**
     * Verifica si el usuario puede aumentar presupuesto.
     */
    fun canIncreaseBudget(): Boolean {
        return currentUserRole == "admin" || currentUserRole == "consignatario"
    }

    /**
     * Verifica si el usuario puede cambiar el estado del viaje.
     */
    fun canChangeStatus(): Boolean {
        return currentUserRole == "admin" || currentUserRole == "consignatario"
    }

    /**
     * Verifica si el formulario de aumento es válido.
     */
    fun isIncreaseBudgetFormValid(): Boolean {
        return newBudgetAmount.isNotBlank() &&
                increaseReason.isNotBlank() &&
                newBudgetAmountError == null &&
                increaseReasonError == null
    }

    /**
     * Calcula el aumento de presupuesto.
     */
    fun getIncreaseAmount(): Double {
        val newAmount = newBudgetAmount.toDoubleOrNull() ?: 0.0
        val currentAmount = trip?.budget?.current ?: 0.0
        return newAmount - currentAmount
    }
}