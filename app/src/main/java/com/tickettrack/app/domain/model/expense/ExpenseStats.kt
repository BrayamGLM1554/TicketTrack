package com.tickettrack.app.domain.model.expense

/**
 * Estadísticas de gastos para gráficas
 */
data class ExpenseStats(
    val totalAmount: Double,
    val expenseCount: Int,
    val averageAmount: Double,
    val expensesByCategory: Map<ExpenseCategory, Double>,
    val expensesByDate: List<DateExpense>
) {
    /**
     * Obtiene el total formateado
     */
    fun getFormattedTotal(): String {
        return "$${"%.2f".format(totalAmount)}"
    }

    /**
     * Obtiene el promedio formateado
     */
    fun getFormattedAverage(): String {
        return "$${"%.2f".format(averageAmount)}"
    }

    /**
     * Obtiene la categoría con mayor gasto
     */
    fun getTopCategory(): ExpenseCategory? {
        return expensesByCategory.maxByOrNull { it.value }?.key
    }
}

/**
 * Gasto agrupado por fecha para gráficas
 */
data class DateExpense(
    val date: String, // Formato: "2025-10-23"
    val amount: Double,
    val count: Int
)

/**
 * Período de tiempo para filtrar gastos
 */
enum class TimePeriod(val displayName: String) {
    WEEK("Semana"),
    MONTH("Mes"),
    YEAR("Año");

    companion object {
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }

        fun fromDisplayName(displayName: String): TimePeriod {
            return values().find { it.displayName == displayName } ?: MONTH
        }
    }
}