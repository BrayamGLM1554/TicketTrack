package com.tickettrack.app.data.model.expense

import com.tickettrack.app.domain.model.expense.*
import java.time.Instant

/**
 * DTO de respuesta del API para un gasto
 * Mapea exactamente la estructura de Firebase
 */
data class ExpenseResponse(
    val id: String,
    val tripId: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val description: String,
    val date: String,

    // Evidencia
    val ticketImagePath: String? = null,
    val ticketPublicUrl: String? = null,
    val ticketModerationStatus: String = "pending",
    val moderationNote: String = "",
    val moderatedAt: String? = null,
    val moderatedBy: String? = null,

    // Ubicación
    val location: LocationDTO? = null,

    // Creador
    val createdBy: String,
    val createdByName: String,

    // Timestamps
    val createdAt: String,
    val updatedAt: String
) {
    /**
     * Convierte el DTO a modelo de dominio
     */
    fun toDomain(): Expense {
        return Expense(
            id = id,
            tripId = tripId,
            amount = amount,
            currency = currency,
            category = ExpenseCategory.fromApiValue(category),
            description = description,
            date = Instant.parse(date),
            ticketImagePath = ticketImagePath,
            ticketPublicUrl = ticketPublicUrl,
            ticketModerationStatus = ModerationStatus.fromApiValue(ticketModerationStatus),
            moderationNote = moderationNote,
            moderatedAt = moderatedAt?.let { Instant.parse(it) },
            moderatedBy = moderatedBy,
            location = location?.let {
                Location(
                    address = it.address,
                    coordinates = Coordinates(
                        latitude = it.coordinates.latitude,
                        longitude = it.coordinates.longitude
                    )
                )
            },
            createdBy = createdBy,
            createdByName = createdByName,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt)
        )
    }
}