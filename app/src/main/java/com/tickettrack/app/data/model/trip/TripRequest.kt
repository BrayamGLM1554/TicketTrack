package com.tickettrack.app.data.model.trip

import com.google.gson.annotations.SerializedName

/**
 * Request para crear un viaje en la API real.
 * Campos simplificados que coinciden EXACTAMENTE con la API.
 */
data class TripRequest(
    @SerializedName("Origin")
    val origin: String,

    @SerializedName("Destination")
    val destination: String,

    @SerializedName("Cargo")
    val cargo: String,

    @SerializedName("TripName")
    val tripName: String,

    @SerializedName("BudgetAssigned")
    val budgetAssigned: Double,

    @SerializedName("TransportistaUid")
    val transportistaUid: String
)