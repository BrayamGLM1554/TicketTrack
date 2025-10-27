package com.tickettrack.app.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de petición para actualizar el estado de un viaje.
 *
 * Ejemplos de uso:
 * - Transportista inicia viaje: pending → in_progress
 * - Admin finaliza viaje: in_progress → completed
 * - Admin cancela viaje: pending/in_progress → cancelled
 */
@Serializable
data class UpdateTripStatusRequest(
    @SerialName("tripId")
    val tripId: String,

    @SerialName("newStatus")
    val newStatus: String,                // "pending", "in_progress", "completed", "cancelled"

    @SerialName("changedBy")
    val changedBy: String,                // UID del usuario que hace el cambio

    @SerialName("changedByName")
    val changedByName: String
)