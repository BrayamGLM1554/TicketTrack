package com.tickettrack.app.data.model.trip

/**
 * Niveles de urgencia para aumentos de presupuesto.
 */
enum class Urgency {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    companion object {
        fun fromString(value: String): Urgency {
            return when (value.lowercase()) {
                "low" -> LOW
                "medium" -> MEDIUM
                "high" -> HIGH
                "critical" -> CRITICAL
                else -> MEDIUM
            }
        }
    }

    fun toFirebaseString(): String {
        return when (this) {
            LOW -> "low"
            MEDIUM -> "medium"
            HIGH -> "high"
            CRITICAL -> "critical"
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            LOW -> "Baja"
            MEDIUM -> "Media"
            HIGH -> "Alta"
            CRITICAL -> "Crítica"
        }
    }

    fun getColor(): Long {
        return when (this) {
            LOW -> 0xFF4CAF50      // Verde
            MEDIUM -> 0xFFFFC107   // Amarillo
            HIGH -> 0xFFFF9800     // Naranja
            CRITICAL -> 0xFFF44336 // Rojo
        }
    }
}