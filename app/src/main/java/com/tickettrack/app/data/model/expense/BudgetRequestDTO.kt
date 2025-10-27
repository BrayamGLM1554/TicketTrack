package com.tickettrack.app.data.model.expense

import com.tickettrack.app.domain.model.expense.BudgetRequest
import com.tickettrack.app.domain.model.expense.BudgetRequestStatus
import com.tickettrack.app.domain.model.expense.BudgetUrgency
import java.time.Instant

/**
 * DTO para crear solicitud de aumento de presupuesto
 */
data class BudgetRequestDTO(
    val tripId: String,
    val currentBudget: Double,
    val requestedBudget: Double,
    val reason: String,
    val urgency: String, // API value: "low", "medium", "high", "critical"
    val requestedBy: String,
    val requestedByName: String
)

/**
 * DTO de respuesta para solicitud de presupuesto
 */
data class BudgetRequestResponse(
    val id: String,
    val tripId: String,
    val tripNumber: String,
    val currentBudget: Double,
    val requestedBudget: Double,
    val increaseAmount: Double,
    val reason: String,
    val urgency: String,
    val requestedBy: String,
    val requestedByName: String,
    val status: String,
    val reviewedBy: String? = null,
    val reviewedByName: String? = null,
    val reviewNote: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    /**
     * Convierte a modelo de dominio
     */
    fun toDomain(): BudgetRequest {
        return BudgetRequest(
            id = id,
            tripId = tripId,
            tripNumber = tripNumber,
            currentBudget = currentBudget,
            requestedBudget = requestedBudget,
            increaseAmount = increaseAmount,
            reason = reason,
            urgency = BudgetUrgency.fromApiValue(urgency),
            requestedBy = requestedBy,
            requestedByName = requestedByName,
            status = BudgetRequestStatus.fromApiValue(status),
            reviewedBy = reviewedBy,
            reviewedByName = reviewedByName,
            reviewNote = reviewNote,
            reviewedAt = reviewedAt?.let { Instant.parse(it) },
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }
}

/**
 * DTO para aprobar/rechazar solicitud
 */
data class ReviewBudgetRequestDTO(
    val requestId: String,
    val approved: Boolean,
    val reviewNote: String,
    val reviewedBy: String,
    val reviewedByName: String
)