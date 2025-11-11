package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

data class Expense(
    @SerializedName("id") val id: String,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("driverId") val driverId: String,
    @SerializedName("category") val category: ExpenseCategory,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("ticketImageUrl") val ticketImageUrl: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("status") val status: String = "approved"
)



data class ExpensesResponse(
    @SerializedName("expenses") val expenses: List<Expense>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int
)