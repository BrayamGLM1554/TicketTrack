package com.tickettrack.app.data.model.trip

import com.tickettrack.app.domain.model.trip.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de respuesta del backend/Firebase para un viaje.
 *
 * Siguiendo Clean Architecture:
 * - Este modelo pertenece a la Data Layer
 * - Se deserializa desde Firebase
 * - Se convierte a modelo de dominio (Trip) para usar en la app
 */
@Serializable
data class TripResponse(
    @SerialName("id")
    val id: String = "",

    @SerialName("cargoName")
    val cargoName: String = "",

    @SerialName("origin")
    val origin: LocationData = LocationData("", "", "", "", CoordinatesData(0.0, 0.0)),

    @SerialName("destination")
    val destination: LocationData = LocationData("", "", "", "", CoordinatesData(0.0, 0.0)),

    @SerialName("cargo")
    val cargo: CargoData = CargoData("", 0.0, ""),

    @SerialName("budget")
    val budget: BudgetData = BudgetData(0.0, 0.0),

    @SerialName("assignedDriverId")
    val assignedDriverId: String = "",

    @SerialName("createdByAdminId")
    val createdByAdminId: String = "",

    @SerialName("status")
    val status: String = "pending",

    @SerialName("statusHistory")
    val statusHistory: List<StatusChangeData> = emptyList(),

    @SerialName("totalExpenses")
    val totalExpenses: Double = 0.0,

    @SerialName("remainingBudget")
    val remainingBudget: Double = 0.0,

    @SerialName("expenseCount")
    val expenseCount: Int = 0,

    @SerialName("budgetIncreaseCount")
    val budgetIncreaseCount: Int = 0,

    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("updatedAt")
    val updatedAt: String = "",

    @SerialName("completedAt")
    val completedAt: String? = null
)

@Serializable
data class StatusChangeData(
    @SerialName("status")
    val status: String = "pending",

    @SerialName("changedAt")
    val changedAt: String = "",

    @SerialName("changedBy")
    val changedBy: String = "",

    @SerialName("changedByName")
    val changedByName: String = ""
)

// Extension functions para convertir de Data a Domain

fun TripResponse.toDomain(): Trip {
    return Trip(
        id = this.id,
        cargoName = this.cargoName,
        origin = this.origin.toDomain(),
        destination = this.destination.toDomain(),
        cargo = this.cargo.toDomain(),
        budget = this.budget.toDomain(),
        assignedDriverId = this.assignedDriverId,
        createdByAdminId = this.createdByAdminId,
        status = TripStatus.fromString(this.status),
        statusHistory = this.statusHistory.map { it.toDomain() },
        totalExpenses = this.totalExpenses,
        remainingBudget = this.remainingBudget,
        expenseCount = this.expenseCount,
        budgetIncreaseCount = this.budgetIncreaseCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt
    )
}

fun LocationData.toDomain(): Location {
    return Location(
        address = this.address,
        city = this.city,
        state = this.state,
        zipCode = this.zipCode,
        coordinates = Coordinates(
            latitude = this.coordinates.latitude,
            longitude = this.coordinates.longitude
        )
    )
}

fun CargoData.toDomain(): Cargo {
    return Cargo(
        type = this.type,
        weight = this.weight,
        description = this.description,
        specialRequirements = this.specialRequirements
    )
}

fun BudgetData.toDomain(): Budget {
    return Budget(
        initial = this.initial,
        current = this.current,
        currency = this.currency,
        history = this.history.map { it.toDomain() }
    )
}

fun BudgetIncreaseData.toDomain(): BudgetIncrease {
    return BudgetIncrease(
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
        urgency = Urgency.fromString(this.urgency),
        supportingExpenses = this.supportingExpenses
    )
}

fun StatusChangeData.toDomain(): StatusChange {
    return StatusChange(
        status = TripStatus.fromString(this.status),
        changedAt = this.changedAt,
        changedBy = this.changedBy,
        changedByName = this.changedByName
    )
}