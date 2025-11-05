package com.tickettrack.app.ui.transportista.profile

/**
 * Estado del perfil del transportista.
 *
 * Muestra información personal del transportista en modo solo lectura.
 * Según RF15: Los transportistas solo deben poder ver sus propios datos.
 *
 * HU relevantes: Visualización de datos personales (RF15)
 */
data class TransportistaProfileState(
    // Información personal
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val companyEmail: String = "",

    // Información de la licencia (si está disponible)
    val licenseNumber: String = "",
    val licenseExpiry: String = "",

    // Estadísticas del transportista
    val totalTripsCompleted: Int = 0,
    val totalTripsInProgress: Int = 0,
    val totalExpensesRegistered: Double = 0.0,
    val averageExpensePerTrip: Double = 0.0,
    val memberSince: String = "",

    // Estados de UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)