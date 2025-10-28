package com.tickettrack.app.data.model.trip

import com.tickettrack.app.domain.model.trip.*

/**
 * Mappers para convertir entre modelos de API (simples) y Domain (ricos).
 *
 * Estrategia:
 * - API → Domain: Enriquecemos datos simples a objetos complejos
 * - Domain → API: Simplificamos objetos complejos a strings/números
 */

// ============================================
// API → Domain (lo que recibimos de la API)
// ============================================

/**
 * Convierte TripResponse de API a Trip de Domain.
 */
fun TripResponse.toDomain(): Trip {
    return Trip(
        id = this.id,
        cargoName = this.tripName,

        // Parseamos strings de ubicación a objetos Location
        origin = this.origin.parseToLocation(),
        destination = this.destination.parseToLocation(),

        // Convertimos string de cargo a objeto Cargo
        cargo = Cargo(
            type = "General", // API no provee tipo
            weight = 0.0,     // API no provee peso
            description = this.cargo,
            specialRequirements = null
        ),

        // Convertimos número a objeto Budget
        budget = Budget(
            initial = this.budgetAssigned,
            current = this.budgetAssigned,
            currency = "MXN",
            history = emptyList() // API no provee historial
        ),

        assignedDriverId = this.transportistaUid,
        createdByAdminId = "", // API no provee este campo
        status = TripStatus.fromString(this.status),
        statusHistory = emptyList(), // API no provee historial de estados
        totalExpenses = 0.0,
        remainingBudget = this.budgetAssigned,
        expenseCount = 0,
        budgetIncreaseCount = 0,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = null
    )
}

/**
 * Convierte lista de TripResponse a lista de Trip.
 */
fun List<TripResponse>.toDomain(): List<Trip> {
    return this.map { it.toDomain() }
}

// ============================================
// Domain → API (lo que enviamos a la API)
// ============================================

/**
 * Convierte Trip de Domain a TripRequest de API.
 */
fun Trip.toApiRequest(): TripRequest {
    return TripRequest(
        origin = this.origin.toApiString(),
        destination = this.destination.toApiString(),
        cargo = this.cargo.description,
        tripName = this.cargoName,
        budgetAssigned = this.budget.current,
        transportistaUid = this.assignedDriverId
    )
}

// ============================================
// Helpers de conversión
// ============================================

/**
 * Parsea un string de dirección de la API a objeto Location.
 *
 * Formato API esperado:
 * "Empresa, Calle y Número, Ciudad, Estado, País"
 *
 * Ejemplo:
 * "Whirlpool México, Carretera A Laredo Km. 9.5, Cienega De Flores, Nuevo León, México"
 */
private fun String.parseToLocation(): Location {
    val parts = this.split(",").map { it.trim() }

    return when {
        // 5+ partes: Empresa, Calle, Ciudad, Estado, País
        parts.size >= 5 -> Location(
            address = "${parts[0]}, ${parts[1]}",
            city = parts[2],
            state = parts[3],
            zipCode = "",
            coordinates = Coordinates(0.0, 0.0)
        )
        // 4 partes: Empresa, Calle, Ciudad, Estado
        parts.size == 4 -> Location(
            address = "${parts[0]}, ${parts[1]}",
            city = parts[2],
            state = parts[3],
            zipCode = "",
            coordinates = Coordinates(0.0, 0.0)
        )
        // 3 partes: Calle, Ciudad, Estado
        parts.size == 3 -> Location(
            address = parts[0],
            city = parts[1],
            state = parts[2],
            zipCode = "",
            coordinates = Coordinates(0.0, 0.0)
        )
        // 2 partes: Ciudad, Estado
        parts.size == 2 -> Location(
            address = "",
            city = parts[0],
            state = parts[1],
            zipCode = "",
            coordinates = Coordinates(0.0, 0.0)
        )
        // Solo 1 parte: usar todo como address
        else -> Location(
            address = this,
            city = "",
            state = "",
            zipCode = "",
            coordinates = Coordinates(0.0, 0.0)
        )
    }
}

/**
 * Convierte Location de Domain a string para la API.
 */
private fun Location.toApiString(): String {
    return buildString {
        if (address.isNotBlank()) append(address)
        if (city.isNotBlank()) {
            if (isNotEmpty()) append(", ")
            append(city)
        }
        if (state.isNotBlank()) {
            if (isNotEmpty()) append(", ")
            append(state)
        }
    }.ifBlank { "Sin ubicación" }
}