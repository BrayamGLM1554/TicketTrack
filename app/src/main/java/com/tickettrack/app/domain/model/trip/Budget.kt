package com.tickettrack.app.domain.model.trip

import com.tickettrack.app.domain.model.trip.BudgetIncrease

/**
 * Información del presupuesto del viaje con historial de aumentos.
 */
data class Budget(
    val initial: Double = 0.0,
    val current: Double = 0.0,
    val currency: String = "MXN",
    val history: List<BudgetIncrease> = emptyList()
) {
    fun getInitialFormatted(): String {
        return "$%.2f $currency".format(initial)
    }

    fun getCurrentFormatted(): String {
        return "$%.2f $currency".format(current)
    }

    fun getTotalIncreases(): Double {
        return current - initial
    }

    fun getTotalIncreasesFormatted(): String {
        val total = getTotalIncreases()
        return if (total > 0) {
            "+$%.2f $currency".format(total)
        } else {
            "$%.2f $currency".format(total)
        }
    }

    fun getIncreaseCount(): Int {
        return history.size
    }

    fun hasIncreases(): Boolean {
        return history.isNotEmpty()
    }
}