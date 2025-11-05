package com.tickettrack.app.ui.transportista.mytrips

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus

/**
 * Estado de la lista de viajes asignados al transportista.
 *
 * Muestra solo los viajes donde assignedDriverId = userEmail.
 * Permite filtrar por estado y buscar por texto.
 *
 * HU relevantes: HU11 (Consultar Detalles), HU14 (Validación de Roles)
 */
data class MyTripsState(
    // Datos del usuario
    val currentUserId: String = "",
    val currentUserName: String = "",

    // Lista de viajes
    val trips: List<Trip> = emptyList(),
    val filteredTrips: List<Trip> = emptyList(),

    // Filtros
    val selectedStatusFilter: TripStatus? = null,
    val searchQuery: String = "",
    val showFilters: Boolean = false,

    // Estadísticas rápidas
    val totalTrips: Int = 0,
    val tripsInProgress: Int = 0,
    val tripsPending: Int = 0,
    val tripsCompleted: Int = 0,

    // Estados de UI
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Obtiene los viajes a mostrar después de aplicar filtros.
     */
    fun getDisplayTrips(): List<Trip> {
        var result = trips

        // Filtrar por estado si hay uno seleccionado
        selectedStatusFilter?.let { status ->
            result = result.filter { it.status == status }
        }

        // Filtrar por búsqueda
        if (searchQuery.isNotBlank()) {
            result = result.filter { trip ->
                trip.cargoName.contains(searchQuery, ignoreCase = true) ||
                        trip.origin.city.contains(searchQuery, ignoreCase = true) ||
                        trip.destination.city.contains(searchQuery, ignoreCase = true) ||
                        trip.id.contains(searchQuery, ignoreCase = true)
            }
        }

        return result
    }

    /**
     * Indica si hay filtros activos.
     */
    fun hasActiveFilters(): Boolean {
        return selectedStatusFilter != null || searchQuery.isNotBlank()
    }
}