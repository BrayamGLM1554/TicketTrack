package com.tickettrack.app.data.remote.api

import com.tickettrack.app.domain.model.*
import retrofit2.http.*

interface BudgetRequestsApi {

    @GET("api/budget-requests")
    suspend fun getBudgetRequests(
        @Header("Authorization") token: String,
        @Query("status_filter") statusFilter: String? = null,
        @Query("trip_id") tripId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): BudgetRequestsResponse

    @GET("api/budget-requests/pending")
    suspend fun getPendingBudgetRequests(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): BudgetRequestsResponse

    @GET("api/budget-requests/{id}")
    suspend fun getBudgetRequestById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): BudgetRequestDetailResponse

    @PUT("api/budget-requests/{id}/approve")
    suspend fun approveBudgetRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ApproveBudgetRequest
    ): BudgetActionResponse

    @PUT("api/budget-requests/{id}/reject")
    suspend fun rejectBudgetRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: RejectBudgetRequest
    ): BudgetActionResponse
}