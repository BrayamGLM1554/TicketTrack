package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

data class Trip(
    @SerializedName("id")
    val id: String,

    @SerializedName("tripName")
    val tripName: String = "Sin nombre",

    @SerializedName("origin")
    val origin: String = "Sin origen",

    @SerializedName("destination")
    val destination: String = "Sin destino",

    @SerializedName("cargo")
    val cargo: String = "Sin descripción",

    @SerializedName("status")
    val status: TripStatus? = null,

    @SerializedName("transportistaUid")
    val transportistaUid: String? = null,

    @SerializedName("transportistaName")
    val transportistaName: String = "Sin asignar",

    @SerializedName("budgetAssigned")
    val budgetAssigned: Double = 0.0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

enum class TripStatus {
    @SerializedName("PENDING")
    PENDING,
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("COMPLETED")
    COMPLETED
}

data class CreateTripRequest(
    @SerializedName("Origin") val origin: String,
    @SerializedName("Destination") val destination: String,
    @SerializedName("Cargo") val cargo: String,
    @SerializedName("TripName") val tripName: String,
    @SerializedName("BudgetAssigned") val budgetAssigned: Double,
    @SerializedName("TransportistaUid") val transportistaUid: String
)

data class TransportistaAvailable(
    val uid: String,
    val name: String,
    val email: String,
    val profileFilePath: String?,
    val profileImageUrl: String?
)