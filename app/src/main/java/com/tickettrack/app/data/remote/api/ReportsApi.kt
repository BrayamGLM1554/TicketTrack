package com.tickettrack.app.data.remote.api

import com.tickettrack.app.domain.model.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ReportsApi {
    @GET("api/reports/api/reports/dashboard")
    suspend fun getDashboard(
        @Header("Authorization") token: String,
        @Query("period") period: String = "month"
    ): Response<ApiResponse<DashboardResponse>>
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val detail: String? = null
)