package com.tickettrack.app.data.repository

import com.tickettrack.app.data.remote.api.ReportsApi
import com.tickettrack.app.domain.model.DashboardResponse
import com.tickettrack.app.domain.repository.IReportsRepository

class ReportsRepository(
    private val api: ReportsApi
) : IReportsRepository {

    override suspend fun getDashboard(token: String, period: String): Result<DashboardResponse> {
        return try {
            val response = api.getDashboard("Bearer $token", period)

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No data received"))
            } else {
                val errorMessage = response.body()?.detail
                    ?: response.errorBody()?.string()
                    ?: "Unknown error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}