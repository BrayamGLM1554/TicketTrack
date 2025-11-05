package com.tickettrack.app.data.model.driver

data class Driver(
    val id: String = "",
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
    val totalTrips: Int = 0,
    val trips: List<Trip> = emptyList()
)

data class Trip(
    val id: String,
    val title: String,
    val tripNumber: String,
    val origin: String,
    val destination: String,
    val date: String,
    val amount: Double,
    val status: TripStatus
)

enum class TripStatus {
    COMPLETADO,
    EN_CURSO,
    PENDIENTE,
    CANCELADO
}