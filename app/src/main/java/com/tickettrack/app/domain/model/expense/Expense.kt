package com.tickettrack.app.domain.model.expense

import com.tickettrack.app.domain.model.trip.Location
import java.time.Instant

/**
 * Modelo de dominio para un gasto del viaje
 * Mapea la estructura de Firebase: trips/{tripId}/expenses/{expenseId}
 */
data class Expense(
    val id: String,
    val tripId: String,
    val amount: Double,
    val currency: String = "MXN",
    val category: ExpenseCategory,
    val description: String,
    val date: String,

    // Evidencia - Ticket
    val ticketImagePath: String? = null,
    val ticketPublicUrl: String? = null,
    val ticketModerationStatus: ModerationStatus = ModerationStatus.PENDING,
    val moderationNote: String = "",
    val moderatedAt: String = "",
    val moderatedBy: String? = null,

    // Ubicación (opcional)
    val location: Location? = null,

    // Info del creador (denormalizado)
    val createdBy: String,
    val createdByName: String,

    // Timestamps
    val createdAt: String = Instant.now().toString(),
    val updatedAt: String = Instant.now().toString()
) {
    /**
     * Verifica si el gasto tiene ticket aprobado
     */
    fun hasApprovedTicket(): Boolean {
        return ticketPublicUrl != null && ticketModerationStatus == ModerationStatus.APPROVED
    }

    /**
     * Verifica si el ticket está pendiente de moderación
     */
    fun isPendingModeration(): Boolean {
        return ticketModerationStatus == ModerationStatus.PENDING
    }

    /**
     * Obtiene el monto formateado con símbolo de moneda
     */
    fun getFormattedAmount(): String {
        return "$${"%.2f".format(amount)} $currency"
    }
}