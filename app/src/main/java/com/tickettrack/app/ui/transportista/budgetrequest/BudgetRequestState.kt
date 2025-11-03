package com.tickettrack.app.ui.transportista.budgetrequest

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.Urgency

/**
 * Estado para la pantalla de solicitud de aumento de presupuesto.
 *
 * Permite al transportista:
 * - Seleccionar el viaje para el cual solicita aumento
 * - Ingresar el nuevo monto solicitado
 * - Justificar la razón del aumento
 * - Seleccionar nivel de urgencia
 *
 * HU relevantes: HU13 (Aumentar Presupuesto), HU20 (Solicitudes)
 */
data class BudgetRequestState(
    // Datos del usuario
    val currentUserId: String = "",
    val currentUserName: String = "",

    // Viaje seleccionado
    val selectedTrip: Trip? = null,
    val availableTrips: List<Trip> = emptyList(),

    // Datos del formulario
    val currentBudget: Double = 0.0,
    val requestedBudget: String = "",
    val increaseAmount: Double = 0.0,
    val reason: String = "",
    val urgency: Urgency = Urgency.MEDIUM,

    // Errores de validación
    val requestedBudgetError: String? = null,
    val reasonError: String? = null,
    val tripError: String? = null,

    // Estados de UI
    val isLoadingTrips: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Dropdown
    val showTripDropdown: Boolean = false
) {
    /**
     * Calcula el monto del aumento solicitado.
     */
    fun calculateIncrease(): Double {
        val requested = requestedBudget.toDoubleOrNull() ?: 0.0
        return requested - currentBudget
    }

    /**
     * Verifica si el formulario es válido.
     */
    fun isFormValid(): Boolean {
        return selectedTrip != null &&
                requestedBudgetError == null &&
                reasonError == null &&
                requestedBudget.isNotBlank() &&
                reason.isNotBlank()
    }

    /**
     * Verifica si el aumento es válido (mayor al presupuesto actual).
     */
    fun isIncreaseValid(): Boolean {
        val requested = requestedBudget.toDoubleOrNull() ?: 0.0
        return requested > currentBudget
    }

    /**
     * Obtiene el porcentaje de aumento solicitado.
     */
    fun getIncreasePercentage(): Float {
        if (currentBudget <= 0) return 0f
        val increase = calculateIncrease()
        return ((increase / currentBudget) * 100).toFloat()
    }
}