package com.tickettrack.app.data.model.trip

import com.tickettrack.app.domain.model.trip.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de petición para crear un nuevo viaje.
 * Se envía al backend/Firebase para registrar un viaje.
 *
 * Siguiendo Clean Architecture:
 * - Este modelo pertenece a la Data Layer
 * - Se serializa para comunicación con el backend
 * - Puede convertirse a modelo de dominio (Trip)
 */
@Serializable
data class TripRequest(
    @SerialName("cargoName")
    val cargoName: String,

    @SerialName("origin")
    val origin: LocationData,

    @SerialName("destination")
    val destination: LocationData,

    @SerialName("cargo")
    val cargo: CargoData,

    @SerialName("budget")
    val budget: BudgetData,

    @SerialName("assignedDriverId")
    val assignedDriverId: String,

    @SerialName("createdByAdminId")
    val createdByAdminId: String
)

@Serializable
data class LocationData(
    @SerialName("address")
    val address: String,

    @SerialName("city")
    val city: String,

    @SerialName("state")
    val state: String,

    @SerialName("zipCode")
    val zipCode: String,

    @SerialName("coordinates")
    val coordinates: CoordinatesData
)

@Serializable
data class CoordinatesData(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double
)

@Serializable
data class CargoData(
    @SerialName("type")
    val type: String,

    @SerialName("weight")
    val weight: Double,

    @SerialName("description")
    val description: String,

    @SerialName("specialRequirements")
    val specialRequirements: String? = null
)

@Serializable
data class BudgetData(
    @SerialName("initial")
    val initial: Double,

    @SerialName("current")
    val current: Double,

    @SerialName("currency")
    val currency: String = "MXN",

    @SerialName("history")
    val history: List<BudgetIncreaseData> = emptyList()
)

@Serializable
data class BudgetIncreaseData(
    @SerialName("previousAmount")
    val previousAmount: Double,

    @SerialName("newAmount")
    val newAmount: Double,

    @SerialName("increase")
    val increase: Double,

    @SerialName("reason")
    val reason: String,

    @SerialName("requestedBy")
    val requestedBy: String,

    @SerialName("requestedByName")
    val requestedByName: String,

    @SerialName("approvedBy")
    val approvedBy: String,

    @SerialName("approvedByName")
    val approvedByName: String,

    @SerialName("requestedAt")
    val requestedAt: String,

    @SerialName("approvedAt")
    val approvedAt: String,

    @SerialName("urgency")
    val urgency: String,

    @SerialName("supportingExpenses")
    val supportingExpenses: List<String> = emptyList()
)

// Extension functions para convertir entre modelos de dominio y data

fun Location.toLocationData(): LocationData {
    return LocationData(
        address = this.address,
        city = this.city,
        state = this.state,
        zipCode = this.zipCode,
        coordinates = CoordinatesData(
            latitude = this.coordinates.latitude,
            longitude = this.coordinates.longitude
        )
    )
}

fun Cargo.toCargoData(): CargoData {
    return CargoData(
        type = this.type,
        weight = this.weight,
        description = this.description,
        specialRequirements = this.specialRequirements
    )
}

fun Budget.toBudgetData(): BudgetData {
    return BudgetData(
        initial = this.initial,
        current = this.current,
        currency = this.currency,
        history = this.history.map { it.toBudgetIncreaseData() }
    )
}

fun BudgetIncrease.toBudgetIncreaseData(): BudgetIncreaseData {
    return BudgetIncreaseData(
        previousAmount = this.previousAmount,
        newAmount = this.newAmount,
        increase = this.increase,
        reason = this.reason,
        requestedBy = this.requestedBy,
        requestedByName = this.requestedByName,
        approvedBy = this.approvedBy,
        approvedByName = this.approvedByName,
        requestedAt = this.requestedAt,
        approvedAt = this.approvedAt,
        urgency = this.urgency.toFirebaseString(),
        supportingExpenses = this.supportingExpenses
    )
}