package com.tickettrack.app.domain.model.expense

/**
 * Categorías de gastos según Firebase
 * fuel | food | toll | maintenance | other
 */
enum class ExpenseCategory(val displayName: String, val apiValue: String) {
    FUEL("Combustible", "fuel"),
    FOOD("Alimentos", "food"),
    TOLL("Casetas", "toll"),
    MAINTENANCE("Mantenimiento", "maintenance"),
    OTHER("Otros", "other");

    companion object {
        /**
         * Obtiene la categoría desde el valor de la API
         */
        fun fromApiValue(value: String): ExpenseCategory {
            return values().find { it.apiValue == value } ?: OTHER
        }

        /**
         * Obtiene todas las categorías como lista de strings para UI
         */
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }

        /**
         * Obtiene la categoría desde el nombre de display
         */
        fun fromDisplayName(displayName: String): ExpenseCategory {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}