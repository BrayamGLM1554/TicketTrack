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
        return "$address, $city, $state $zipCode"
    }

    fun getShortAddress(): String {
        return "$city, $state"
    }

    fun isValid(): Boolean {
        return address.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                zipCode.isNotBlank()
    }
}