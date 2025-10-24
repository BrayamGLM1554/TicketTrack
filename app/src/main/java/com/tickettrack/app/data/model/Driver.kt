package com.tickettrack.app.data.model

data class Driver(
    val fullName: String,
    val email: String,
    val licenseNumber: String,
    val licenseCaducation: String,
    val personalPhoneEncrypted: String,
    val createdAt: String,
    val createdBy: String,
    val companyName: String,
    val isActive: Boolean = true,
    val unitNumber: String = "",
    val totalTrips: Int = 0
)