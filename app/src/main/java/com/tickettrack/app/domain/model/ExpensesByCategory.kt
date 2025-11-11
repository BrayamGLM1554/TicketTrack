package com.tickettrack.app.domain.model
data class ExpensesByCategory(
    val category: ExpenseCategory,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
)