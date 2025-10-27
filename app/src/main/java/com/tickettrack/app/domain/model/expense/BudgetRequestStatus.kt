package com.tickettrack.app.domain.model.expense

/**
 * Estado de solicitud de aumento de presupuesto
 */
enum class BudgetRequestStatus(val displayName: String, val apiValue: String) {
    PENDING("Pendiente", "pending"),
    APPROVED("Aprobada", "approved"),
    REJECTED("Rechazada", "rejected");

    companion object {
        /**
         * Obtiene el estado desde el valor de la API
         */
        fun fromApiValue(value: String): BudgetRequestStatus {
            return values().find { it.apiValue == value } ?: PENDING
        }
    }

    /**
     * Verifica si el estado permite edición
     */
    fun canEdit(): Boolean {
        return this == PENDING
    }

    /**
     * Verifica si fue resuelta (aprobada o rechazada)
     */
    fun isResolved(): Boolean {
        return this == APPROVED || this == REJECTED
    }
}