package com.tickettrack.app.data.model.trip

/**
 * Coordenadas geográficas.
 */
data class Coordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun isValid(): Boolean {
        return latitude != 0.0 && longitude != 0.0 &&
                latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0
    }

    fun toMapUrl(): String {
        return "https://www.google.com/maps?q=$latitude,$longitude"
    }
}