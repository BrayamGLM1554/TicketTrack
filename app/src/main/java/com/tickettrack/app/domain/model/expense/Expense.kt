package com.tickettrack.app.domain.model.expense

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
    val date: Instant,

    // Evidencia - Ticket
    val ticketImagePath: String? = null,
    val ticketPublicUrl: String? = null,
    val ticketModerationStatus: ModerationStatus = ModerationStatus.PENDING,
    val moderationNote: String = "",
    val moderatedAt: Instant? = null,
    val moderatedBy: String? = null,

    // Ubicación (opcional)
    val location: Location? = null,

    // Info del creador (denormalizado)
    val createdBy: String,
    val createdByName: String,

    // Timestamps
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
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