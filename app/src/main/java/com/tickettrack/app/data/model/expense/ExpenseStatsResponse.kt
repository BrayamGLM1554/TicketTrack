package com.tickettrack.app.data.model.expense

import com.tickettrack.app.domain.model.expense.*

/**
 * DTO de respuesta para estadísticas de gastos
 */
data class ExpenseStatsResponse(
    val totalAmount: Double,
    val expenseCount: Int,
    val averageAmount: Double,
    val expensesByCategory: Map<String, Double>, // API values: "fuel": 500.0
    val expensesByDate: List<DateExpenseDTO>
) {
    /**
     * Convierte a modelo de dominio
     */
    fun toDomain(): ExpenseStats {
        // Convertir las categorías de API values a enum
        val categoriesMap = expensesByCategory.mapKeys { (key, _) ->
            ExpenseCategory.fromApiValue(key)
        }

        val dateExpenses = expensesByDate.map { dto ->
            DateExpense(
                date = dto.date,
                amount = dto.amount,
                count = dto.count
            )
        }

        return ExpenseStats(
            totalAmount = totalAmount,
            expenseCount = expenseCount,
            averageAmount = averageAmount,
            expensesByCategory = categoriesMap,
            expensesByDate = dateExpenses
        )
    }
}

/**
 * DTO para gasto agrupado por fecha
 */
data class DateExpenseDTO(
    val date: String,
    val amount: Double,
    val count: Int
)