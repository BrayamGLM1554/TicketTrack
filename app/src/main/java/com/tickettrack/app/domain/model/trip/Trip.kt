package com.tickettrack.app.domain.model.trip

/**
 * Modelo de dominio de un Viaje.
 * Representa toda la información de un viaje en la aplicación.
 */
data class Trip(
    val id: String = "",
    val cargoName: String = "",
    val origin: Location = Location(),
    val destination: Location = Location(),
    val cargo: Cargo = Cargo(),
    val budget: Budget = Budget(),
    val assignedDriverId: String = "",
    val createdByAdminId: String = "",
    val status: TripStatus = TripStatus.PENDING,
    val statusHistory: List<StatusChange> = emptyList(),
    val totalExpenses: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val expenseCount: Int = 0,
    val budgetIncreaseCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val completedAt: String? = null
) {
    fun getTotalExpensesFormatted(): String {
        return "$%.2f ${budget.currency}".format(totalExpenses)
    }

    fun getRemainingBudgetFormatted(): String {
        return "$%.2f ${budget.currency}".format(remainingBudget)
    }

    fun getBudgetUsagePercentage(): Double {
        return if (budget.current > 0) {
            (totalExpenses / budget.current) * 100
        } else {
            0.0
        }
    }

    fun getBudgetUsagePercentageFormatted(): String {
        return "%.1f%%".format(getBudgetUsagePercentage())
    }

    fun isOverBudget(): Boolean {
        return totalExpenses > budget.current
    }

    fun canIncreaseBudget(): Boolean {
        return status != TripStatus.COMPLETED && status != TripStatus.CANCELLED
    }

    fun canFinalize(): Boolean {
        return status == TripStatus.IN_PROGRESS
    }

    fun canStart(): Boolean {
        return status == TripStatus.PENDING
    }

    fun canCancel(): Boolean {
        return status == TripStatus.PENDING || status == TripStatus.IN_PROGRESS
    }

    fun getStatusDisplayName(): String {
        return status.getDisplayName()
    }

    fun getStatusIcon(): String {
        return status.getIcon()
    }

    fun getShortDisplayName(): String {
        return "$cargoName - ${origin.city} → ${destination.city}"
    }
}