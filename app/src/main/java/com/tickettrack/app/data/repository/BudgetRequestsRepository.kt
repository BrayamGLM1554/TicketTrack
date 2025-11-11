package com.tickettrack.app.data.repository

import com.tickettrack.app.data.remote.api.BudgetRequestsApi
import com.tickettrack.app.domain.model.*
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository

class BudgetRequestsRepository(
    private val api: BudgetRequestsApi
) : IBudgetRequestsRepository {

    override suspend fun getPendingBudgetRequests(
        token: String,
        page: Int,
        limit: Int
    ): Result<BudgetRequestsResponse> {
        return try {
            val response = api.getPendingBudgetRequests("Bearer $token", page, limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBudgetRequestById(
        token: String,
        id: String
    ): Result<BudgetRequest> {
        return try {
            val response = api.getBudgetRequestById("Bearer $token", id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveBudgetRequest(
        token: String,
        id: String,
        request: ApproveBudgetRequest
    ): Result<BudgetRequest> {
        return try {
            val response = api.approveBudgetRequest("Bearer $token", id, request)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectBudgetRequest(
        token: String,
        id: String,
        request: RejectBudgetRequest
    ): Result<BudgetRequest> {
        return try {
            val response = api.rejectBudgetRequest("Bearer $token", id, request)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}