package com.tickettrack.app.domain.repository

import com.tickettrack.app.domain.model.DashboardResponse

interface IReportsRepository {
    suspend fun getDashboard(token: String, period: String): Result<DashboardResponse>
}