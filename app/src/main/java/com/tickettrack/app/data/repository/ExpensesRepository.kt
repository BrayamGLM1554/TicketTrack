package com.tickettrack.app.data.repository

import com.tickettrack.app.data.remote.api.ExpensesApi
import com.tickettrack.app.domain.model.*
import com.tickettrack.app.domain.repository.IExpensesRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ExpensesRepository(
    private val api: ExpensesApi
) : IExpensesRepository {

    override suspend fun getExpenses(
        token: String,
        tripId: String?,
        category: String?,
        page: Int,
        limit: Int
    ): Result<ExpensesResponse> {
        return try {
            val expenses = api.getExpenses("Bearer $token", tripId, category, page, limit)
            val response = ExpensesResponse(
                expenses = expenses,
                total = expenses.size,
                page = page,
                limit = limit
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyExpenses(token: String): Result<List<Expense>> {
        return try {
            val response = api.getMyExpenses("Bearer $token")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpenseById(token: String, expenseId: String): Result<Expense> {
        return try {
            val response = api.getExpenseById("Bearer $token", expenseId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createExpense(
        token: String,
        tripId: String,
        driverId: String,
        category: String,
        amount: Double,
        description: String,
        imageFile: File?
    ): Result<CreateExpenseResponse> {
        return try {
            val tripIdBody = tripId.toRequestBody("text/plain".toMediaTypeOrNull())
            val driverIdBody = driverId.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val amountBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("TicketImage", it.name, requestFile)
            }

            val response = api.createExpense(
                token = "Bearer $token",
                tripId = tripIdBody,
                driverId = driverIdBody,
                category = categoryBody,
                amount = amountBody,
                description = descriptionBody,
                ticketImage = imagePart
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}