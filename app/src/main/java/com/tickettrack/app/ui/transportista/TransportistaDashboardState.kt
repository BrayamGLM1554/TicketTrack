package com.tickettrack.app.ui.transportista

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.expense.Expense

/**
 * Estado del Dashboard del Transportista (USER).
 *
 * Muestra resumen de:
 * - Viajes asignados y su estado
 * - Gastos del período actual
 * - Alertas y notificaciones
 * - Accesos rápidos a funciones principales
 */
data class TransportistaDashboardState(
    // Información del usuario
    val userName: String = "",
    val userEmail: String = "",

    // Resumen de viajes
    val totalTripsAssigned: Int = 0,
    val tripsInProgress: Int = 0,
    val tripsPending: Int = 0,
    val tripsCompleted: Int = 0,

    // Resumen financiero
    val totalExpenses: Double = 0.0,
    val totalBudgetAssigned: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val budgetUsagePercentage: Float = 0f,

    // Datos para cards
    val recentTrips: List<Trip> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),

    // Alertas
    val hasLowBudgetAlert: Boolean = false,
    val hasPendingRequests: Int = 0,

    // Estados de UI
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)