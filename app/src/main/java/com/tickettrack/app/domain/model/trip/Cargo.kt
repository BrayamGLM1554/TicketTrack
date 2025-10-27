package com.tickettrack.app.domain.model.trip

/**
 * Información de la carga del viaje.
 */
data class Cargo(
    val type: String = "",                    // "Maquinaria", "Alimentos", etc.
    val weight: Double = 0.0,                 // kg
    val description: String = "",
    val specialRequirements: String? = null
) {
    fun getWeightFormatted(): String {
        return "$weight kg"
    }

    fun getWeightInTons(): Double {
        return weight / 1000.0
    }

    fun getWeightInTonsFormatted(): String {
        val tons = getWeightInTons()
        return if (tons >= 1.0) {
            "%.2f ton".format(tons)
        } else {
            getWeightFormatted()
        }
    }

    fun isValid(): Boolean {
        return type.isNotBlank() &&
                weight > 0 &&
                description.isNotBlank()
    }
}