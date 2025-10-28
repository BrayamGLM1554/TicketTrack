package com.tickettrack.app.data.model.trip

import com.google.gson.annotations.SerializedName

/**
 * Response de la API real para un viaje.
 * Estructura simple que coincide EXACTAMENTE con la API.
 */
data class TripResponse(
    @SerializedName("Id")
    val id: String = "",

    @SerializedName("Origin")
    val origin: String = "",

    @SerializedName("Destination")
    val destination: String = "",

    @SerializedName("Cargo")
    val cargo: String = "",

    @SerializedName("TripName")
    val tripName: String = "",

    @SerializedName("BudgetAssigned")
    val budgetAssigned: Double = 0.0,

    @SerializedName("TransportistaUid")
    val transportistaUid: String = "",

    @SerializedName("Status")
    val status: String = "pending",

    @SerializedName("CreatedAt")
    val createdAt: String = "",

    @SerializedName("UpdatedAt")
    val updatedAt: String = "",

    @SerializedName("Transportista")
    val transportista: TransportistaResponse? = null
)

// ============================================
// Modelos adicionales para funcionalidades mock
// ============================================

/**
 * Request para aumentar presupuesto (funcionalidad sin endpoint aún).
 */
data class IncreaseBudgetRequest(
    val tripId: String,
    val previousAmount: Double,
    val newAmount: Double,
    val increase: Double,
    val reason: String,
    val requestedBy: String,
    val requestedByName: String,
    val approvedBy: String,
    val approvedByName: String,
    val urgency: String,
    val supportingExpenses: List<String>
)

/**
 * Request para actualizar estado (funcionalidad sin endpoint aún).
 */
data class UpdateTripStatusRequest(
    val tripId: String,
    val newStatus: String,
    val changedBy: String,
    val changedByName: String
)

/**
 * Datos de cambio de presupuesto (para historial mock).
 */
data class BudgetIncreaseData(
    val previousAmount: Double = 0.0,
    val newAmount: Double = 0.0,
    val increase: Double = 0.0,
    val reason: String = "",
    val requestedBy: String = "",
    val requestedByName: String = "",
    val approvedBy: String = "",
    val approvedByName: String = "",
    val requestedAt: String = "",
    val approvedAt: String = "",
    val urgency: String = "",
    val supportingExpenses: List<String> = emptyList()
)

/**
 * Datos de cambio de estado (para historial mock).
 */
data class StatusChangeData(
    val status: String = "pending",
    val changedAt: String = "",
    val changedBy: String = "",
    val changedByName: String = ""
)