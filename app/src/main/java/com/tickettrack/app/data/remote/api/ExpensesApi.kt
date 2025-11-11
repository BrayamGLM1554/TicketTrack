package com.tickettrack.app.data.remote.api

import com.tickettrack.app.domain.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ExpensesApi {

    @GET("api/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @Query("tripId") tripId: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): List<Expense>

    @GET("api/expenses/my-expenses")
    suspend fun getMyExpenses(
        @Header("Authorization") token: String
    ): List<Expense>

    @GET("api/expenses/{id}")
    suspend fun getExpenseById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Expense

    @Multipart
    @POST("api/expenses")
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Part("TripId") tripId: RequestBody,
        @Part("DriverId") driverId: RequestBody,
        @Part("Category") category: RequestBody,
        @Part("Amount") amount: RequestBody,
        @Part("Description") description: RequestBody,
        @Part ticketImage: MultipartBody.Part?
    ): CreateExpenseResponse
}