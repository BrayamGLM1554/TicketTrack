package com.tickettrack.app.data.model.expense

import com.tickettrack.app.domain.model.expense.ExpenseCategory
import java.time.Instant

/**
 * DTO para crear un gasto
 * Se envía al API Gateway
 */
data class ExpenseRequest(
    val tripId: String,
    val amount: Double,
    val currency: String = "MXN",
    val category: String, // API value: "fuel", "food", etc.
    val description: String,
    val date: String, // ISO 8601 format

    // Imagen del ticket (se enviará el base64 o URL temporal)
    val ticketImageBase64: String? = null,

    // Ubicación (opcional)
    val location: LocationDTO? = null,

    // Info del creador
    val createdBy: String,
    val createdByName: String
) {
    companion object {
        /**
         * Convierte categoría de dominio a API value
         */
        fun fromDomain(
            tripId: String,
            amount: Double,
            category: ExpenseCategory,
            description: String,
            date: Instant,
            ticketImageBase64: String?,
            location: LocationDTO?,
            createdBy: String,
            createdByName: String
        ): ExpenseRequest {
            return ExpenseRequest(
                tripId = tripId,
                amount = amount,
                category = category.apiValue,
                description = description,
                date = date.toString(),
                ticketImageBase64 = ticketImageBase64,
                location = location,
                createdBy = createdBy,
                createdByName = createdByName
            )
        }
    }
}

/**
 * DTO para ubicación
 */
data class LocationDTO(
    val address: String,
    val coordinates: CoordinatesDTO
)

/**
 * DTO para coordenadas
 */
data class CoordinatesDTO(
    val latitude: Double,
    val longitude: Double
)