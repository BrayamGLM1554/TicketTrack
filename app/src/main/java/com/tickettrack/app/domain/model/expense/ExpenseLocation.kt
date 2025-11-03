package com.tickettrack.app.domain.model.expense

import com.tickettrack.app.domain.model.trip.Coordinates

/**
 * Ubicación donde se realizó el gasto (opcional)
 */
data class ExpenseLocation(
    val address: String,
    val coordinates: Coordinates
)

/**
 * Coordenadas geográficas
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Verifica si las coordenadas son válidas
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Obtiene las coordenadas en formato string
     */
    fun toStringFormat(): String {
        return "$latitude, $longitude"
    }
}