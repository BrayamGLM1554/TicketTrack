package com.tickettrack.app.domain.model.expense

/**
 * Estado de moderación de imágenes de tickets
 * pending | approved | rejected | flagged
 */
enum class ModerationStatus(val displayName: String, val apiValue: String) {
    PENDING("Pendiente", "pending"),
    APPROVED("Aprobado", "approved"),
    REJECTED("Rechazado", "rejected"),
    FLAGGED("Marcado", "flagged");

    companion object {
        /**
         * Obtiene el estado desde el valor de la API
         */
        fun fromApiValue(value: String): ModerationStatus {
            return values().find { it.apiValue == value } ?: PENDING
        }
    }

    /**
     * Verifica si el estado permite mostrar la imagen
     */
    fun canShowImage(): Boolean {
        return this == APPROVED
    }

    /**
     * Verifica si el estado requiere acción del usuario
     */
    fun requiresAction(): Boolean {
        return this == REJECTED || this == FLAGGED
    }
}