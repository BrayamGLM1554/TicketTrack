package com.tickettrack.app.domain.model.expense

import java.time.Instant

/**
 * Solicitud de aumento de presupuesto para un viaje
 */
data class BudgetRequest(
    val id: String,
    val tripId: String,
    val tripNumber: String,

    // Montos
    val currentBudget: Double,
    val requestedBudget: Double,
    val increaseAmount: Double,

    // Justificación
    val reason: String,
    val urgency: BudgetUrgency,

    // Solicitante (transportista)
    val requestedBy: String,
    val requestedByName: String,

    // Estado
    val status: BudgetRequestStatus = BudgetRequestStatus.PENDING,

    // Respuesta (si fue aprobada/rechazada)
    val reviewedBy: String? = null,
    val reviewedByName: String? = null,
    val reviewNote: String? = null,
    val reviewedAt: Instant? = null,

    // Timestamps
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    /**
     * Calcula el porcentaje de aumento solicitado
     */
    fun getIncreasePercentage(): Double {
        return if (currentBudget > 0) {
            (increaseAmount / currentBudget) * 100
        } else {
            0.0
        }
    }

    /**
     * Verifica si la solicitud está pendiente
     */
    fun isPending(): Boolean {
        return status == BudgetRequestStatus.PENDING
    }

    /**
     * Obtiene el monto de aumento formateado
     */
    fun getFormattedIncrease(): String {
        return "+$${"%.2f".format(increaseAmount)}"
    }

    /**
     * Obtiene el porcentaje formateado
     */
    fun getFormattedPercentage(): String {
        return "+${"%.1f".format(getIncreasePercentage())}%"
    }
}