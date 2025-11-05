package com.tickettrack.app.domain.model.expense

import androidx.compose.ui.graphics.Color

enum class ExpenseCategory(val displayName: String, val apiValue: String) {
    FUEL("Combustible", "fuel"),
    FOOD("Alimentos", "food"),
    TOLL("Casetas", "toll"),
    MAINTENANCE("Mantenimiento", "maintenance"),
    OTHER("Otros", "other");

    /**
     * Devuelve un color representativo para cada categoría
     */
    fun getColor(): Long {
        return when (this) {
            FUEL -> 0xFFFFC107 // Ámbar
            FOOD -> 0xFF4CAF50 // Verde
            TOLL -> 0xFF03A9F4 // Azul claro
            MAINTENANCE -> 0xFFF44336 // Rojo
            OTHER -> 0xFF9E9E9E // Gris
        }
    }

    companion object {
        fun fromApiValue(value: String): ExpenseCategory {
            return values().find { it.apiValue == value } ?: OTHER
        }

        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }

        fun fromDisplayName(displayName: String): ExpenseCategory {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}
