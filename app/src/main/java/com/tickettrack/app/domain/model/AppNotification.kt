package com.tickettrack.app.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de notificación para la app
 */
data class AppNotification(
    val id: String = "",
    val userId: String = "", // UID del usuario (del JWT)
    val type: NotificationType = NotificationType.TRIP_ASSIGNED,
    val title: String = "",
    val message: String = "",
    val relatedId: String = "", // ID del viaje o budget request
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val metadata: Map<String, String> = emptyMap()
) {

    /**
     * Convierte a Map para guardar en Firestore
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "type" to type.name,
            "title" to title,
            "message" to message,
            "relatedId" to relatedId,
            "isRead" to isRead,
            "createdAt" to createdAt,
            "metadata" to metadata
        )
    }

    companion object {

        /**
         * Convierte documento Firestore → AppNotification sin crashear.
         */
        fun fromFirestore(id: String, data: Map<String, Any>): AppNotification {
            val typeString = data["type"] as? String ?: "TRIP_ASSIGNED"

            // Fallback seguro si aparece un tipo inesperado
            val safeType = try {
                NotificationType.valueOf(typeString)
            } catch (e: Exception) {
                NotificationType.GENERAL
            }

            return AppNotification(
                id = id,
                userId = data["userId"] as? String ?: "",
                type = safeType,
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                relatedId = data["relatedId"] as? String ?: "",
                isRead = data["isRead"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                metadata = (data["metadata"] as? Map<String, String>) ?: emptyMap()
            )
        }
    }
}

/**
 * Tipos de notificaciones en la app
 */
enum class NotificationType {
    GENERAL,             // Notificación genérica (evita crashes)
    TRIP_ASSIGNED,       // Nuevo viaje asignado (USER)
    BUDGET_REQUESTED,    // Nueva petición de presupuesto (ADMIN)
    BUDGET_APPROVED,     // Petición aprobada (USER)
    BUDGET_REJECTED      // Petición rechazada (USER)
}
