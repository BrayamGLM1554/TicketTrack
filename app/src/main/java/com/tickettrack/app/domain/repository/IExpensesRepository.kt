package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.*
import java.io.File

interface IExpensesRepository {
    suspend fun getExpenses(
        token: String,
        tripId: String? = null,
        category: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): Result<ExpensesResponse>

    suspend fun getExpenseById(token: String, expenseId: String): Result<Expense>

    suspend fun getMyExpenses(token: String): Result<List<Expense>>

    suspend fun createExpense(
        token: String,
        tripId: String,
        driverId: String,
        category: String,
        amount: Double,
        description: String,
        imageFile: File?
    ): Result<CreateExpenseResponse>
}