package com.tickettrack.app.domain.model.trip

/**
 * Estados posibles de un viaje.
 */
enum class TripStatus {
    PENDING,        // Viaje creado, esperando inicio
    IN_PROGRESS,    // Viaje en curso
    COMPLETED,      // Viaje finalizado
    CANCELLED;      // Viaje cancelado

    companion object {
        fun fromString(value: String): TripStatus {
            return when (value.lowercase()) {
                "pending" -> PENDING
                "in_progress" -> IN_PROGRESS
                "completed" -> COMPLETED
                "cancelled" -> CANCELLED
                else -> PENDING
            }
        }
    }
    fun getColor(): Int {
        return when (this) {
            PENDING -> 0xFFFF9800.toInt()      // Naranja
            IN_PROGRESS -> 0xFF2196F3.toInt()  // Azul
            COMPLETED -> 0xFF4CAF50.toInt()    // Verde
            CANCELLED -> 0xFFF44336.toInt()    // Rojo
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "Pendiente"
            IN_PROGRESS -> "En Progreso"
            COMPLETED -> "Completado"
            CANCELLED -> "Cancelado"
        }
    }

    fun getIcon(): String {
        return when (this) {
            PENDING -> "⏱️"
            IN_PROGRESS -> "🚚"
            COMPLETED -> "✅"
            CANCELLED -> "❌"
        }
    }
}