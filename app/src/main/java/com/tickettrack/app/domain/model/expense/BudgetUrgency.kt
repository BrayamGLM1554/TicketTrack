package com.tickettrack.app.domain.model.expense

/**
 * Nivel de urgencia de la solicitud de presupuesto
 */
enum class BudgetUrgency(val displayName: String, val apiValue: String) {
    LOW("Baja", "low"),
    MEDIUM("Media", "medium"),
    HIGH("Alta", "high"),
    CRITICAL("Crítica", "critical");

    companion object {
        /**
         * Obtiene la urgencia desde el valor de la API
         */
        fun fromApiValue(value: String): BudgetUrgency {
            return values().find { it.apiValue == value } ?: MEDIUM
        }

        /**
         * Obtiene todas las urgencias como lista de strings para UI
         */
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }

        /**
         * Obtiene la urgencia desde el nombre de display
         */
        fun fromDisplayName(displayName: String): BudgetUrgency {
            return values().find { it.displayName == displayName } ?: MEDIUM
        }
    }
}