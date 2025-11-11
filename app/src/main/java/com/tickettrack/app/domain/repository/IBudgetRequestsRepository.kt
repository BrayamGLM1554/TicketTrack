package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.*

interface IBudgetRequestsRepository {
    suspend fun getPendingBudgetRequests(
        token: String,
        page: Int = 1,
        limit: Int = 20
    ): Result<BudgetRequestsResponse>

    suspend fun getBudgetRequestById(
        token: String,
        id: String
    ): Result<BudgetRequest>

    suspend fun approveBudgetRequest(
        token: String,
        id: String,
        request: ApproveBudgetRequest
    ): Result<BudgetRequest>

    suspend fun rejectBudgetRequest(
        token: String,
        id: String,
        request: RejectBudgetRequest
    ): Result<BudgetRequest>
}