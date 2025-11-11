package com.tickettrack.app.domain.model

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class CreateExpenseRequest(
    val tripId: String,
    val driverId: String,
    val category: String,
    val amount: Double,
    val description: String,
    val ticketImage: MultipartBody.Part?
)

data class CreateExpenseResponse(
    val id: String,
    val tripId: String,
    val driverId: String,
    val category: String,
    val amount: Double,
    val description: String,
    val ticketImageUrl: String?,
    val createdAt: String,
    val status: String
)