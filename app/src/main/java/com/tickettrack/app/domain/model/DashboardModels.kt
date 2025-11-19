package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    val overview: OverviewData,
    val trips: TripsData,
    val drivers: DriversData,
    val expenses: ExpensesData,
    val alerts: List<AlertItem>,
    val recentActivity: List<ActivityItem>
)

data class OverviewData(
    val totalBudget: Double,
    val totalSpent: Double,
    val availableBalance: Double,
    val budgetUsedPercentage: Double
)

data class TripsData(
    val active: Int,
    val pending: Int,
    val inProgress: Int,
    val completed: Int,
    val total: Int
)

data class DriversData(
    val active: Int,
    val inactive: Int,
    val onTrip: Int
)

data class ExpensesData(
    val total: Int,
    val totalAmount: Double,
    val averagePerTrip: Double,
    val byCategory: Map<String, Double>
)

data class AlertItem(
    val id: String,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: String,
    val relatedEntity: Map<String, Any>?
)

data class ActivityItem(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val actor: String,
    val entity: Map<String, Any>?
)