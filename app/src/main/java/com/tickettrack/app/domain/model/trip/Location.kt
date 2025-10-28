package com.tickettrack.app.domain.model.trip

import com.tickettrack.app.domain.model.trip.Coordinates

/**
 * Ubicación geográfica con dirección y coordenadas.
 */
data class Location(
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val coordinates: Coordinates = Coordinates()
) {
    fun getFullAddress(): String {
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
            if (zipCode.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(zipCode)
            }
        }.ifBlank { "Ubicación no especificada" }
    }

    fun getShortAddress(): String {
        return buildString {
            if (city.isNotBlank()) append(city)
            if (state.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(state)
            }
        }.ifBlank { "Sin ubicación" }
    }

    fun isValid(): Boolean {
        return address.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                zipCode.isNotBlank()
    }


}