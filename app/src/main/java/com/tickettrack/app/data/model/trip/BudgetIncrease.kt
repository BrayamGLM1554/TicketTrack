package com.tickettrack.app.data.model.trip

/**
 * Representa un aumento de presupuesto en el historial.
 */
data class BudgetIncrease(
    val previousAmount: Double = 0.0,
    val newAmount: Double = 0.0,
    val increase: Double = 0.0,
    val reason: String = "",
    val requestedBy: String = "",              // UID del solicitante
    val requestedByName: String = "",
    val approvedBy: String = "",               // UID del aprobador
    val approvedByName: String = "",
    val requestedAt: String = "",              // ISO 8601
    val approvedAt: String = "",               // ISO 8601
    val urgency: Urgency = Urgency.MEDIUM,
    val supportingExpenses: List<String> = emptyList()  // IDs de gastos
) {
    fun getIncreaseFormatted(): String {
        return "+$%.2f".format(increase)
    }

    fun getIncreasePercentage(): Double {
        return if (previousAmount > 0) {
            (increase / previousAmount) * 100
        } else {
            0.0
        }
    }

    fun getIncreasePercentageFormatted(): String {
        return "+%.1f%%".format(getIncreasePercentage())
    }
}