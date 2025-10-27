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

    fun toFirebaseString(): String {
        return when (this) {
            PENDING -> "pending"
            IN_PROGRESS -> "in_progress"
            COMPLETED -> "completed"
            CANCELLED -> "cancelled"
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