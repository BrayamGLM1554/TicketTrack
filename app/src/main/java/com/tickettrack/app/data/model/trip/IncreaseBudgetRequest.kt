package com.tickettrack.app.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de petición para aumentar el presupuesto de un viaje.
 *
 * Siguiendo principio de Responsabilidad Única (SOLID):
 * - Esta clase solo representa la petición de aumento
 * - No contiene lógica de negocio
 * - Es un simple DTO (Data Transfer Object)
 */
@Serializable
data class IncreaseBudgetRequest(
    @SerialName("tripId")
    val tripId: String,

    @SerialName("previousAmount")
    val previousAmount: Double,

    @SerialName("newAmount")
    val newAmount: Double,

    @SerialName("increase")
    val increase: Double,

    @SerialName("reason")
    val reason: String,

    @SerialName("requestedBy")
    val requestedBy: String,              // UID del solicitante

    @SerialName("requestedByName")
    val requestedByName: String,

    @SerialName("approvedBy")
    val approvedBy: String,               // UID del aprobador

    @SerialName("approvedByName")
    val approvedByName: String,

    @SerialName("urgency")
    val urgency: String,                  // "low", "medium", "high", "critical"

    @SerialName("supportingExpenses")
    val supportingExpenses: List<String> = emptyList()
)