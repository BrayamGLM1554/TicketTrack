package com.tickettrack.app.domain.model

import com.google.gson.annotations.SerializedName

// ============ BUDGET REQUESTS ============

data class BudgetRequest(
    @SerializedName("id") val id: String,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("tripName") val tripName: String,
    @SerializedName("driverId") val driverId: String,
    @SerializedName("driverName") val driverName: String,
    @SerializedName("companyName") val companyName: String,
    @SerializedName("currentBudget") val currentBudget: Double,
    @SerializedName("requestedBudget") val requestedBudget: Double,
    @SerializedName("increaseAmount") val increaseAmount: Double,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: BudgetRequestStatus,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("createdBy") val createdBy: String,
    @SerializedName("approvedBudget") val approvedBudget: Double? = null,
    @SerializedName("approvedBy") val approvedBy: String? = null,
    @SerializedName("approvedAt") val approvedAt: String? = null,
    @SerializedName("approvalNotes") val approvalNotes: String? = null,
    @SerializedName("rejectedBy") val rejectedBy: String? = null,
    @SerializedName("rejectedAt") val rejectedAt: String? = null,
    @SerializedName("rejectionReason") val rejectionReason: String? = null
)

enum class BudgetRequestStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("approved") APPROVED,
    @SerializedName("rejected") REJECTED,
    @SerializedName("cancelled") CANCELLED
}

data class BudgetRequestsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<BudgetRequest>,
    @SerializedName("pagination") val pagination: Pagination
)

data class BudgetRequestDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: BudgetRequest
)

data class ApproveBudgetRequest(
    @SerializedName("approvedBudget") val approvedBudget: Double? = null,
    @SerializedName("approvalNotes") val approvalNotes: String? = null
)

data class RejectBudgetRequest(
    @SerializedName("rejectionReason") val rejectionReason: String
)

data class BudgetActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BudgetRequest
)

