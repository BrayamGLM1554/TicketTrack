package com.tickettrack.app.ui.trips.list

import com.tickettrack.app.domain.model.trip.Trip
import com.tickettrack.app.domain.model.trip.TripStatus

/**
 * Estado de la pantalla de lista de viajes.
 *
 * Siguiendo principios de Compose:
 * - Estado inmutable (data class)
 * - Single source of truth
 * - Toda la UI se renderiza basada en este estado
 */
data class TripListState(
    // Lista de viajes
    val trips: List<Trip> = emptyList(),

    // Estados de carga
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // Filtros
    val selectedStatusFilter: TripStatus? = null,
    val showFilters: Boolean = false,

    // Búsqueda
    val searchQuery: String = "",
    val isSearching: Boolean = false,

    // Error
    val errorMessage: String? = null,

    // Usuario actual
    val currentUserId: String = "",
    val currentUserRole: String = "",  // "admin" o "driver"

    // Estado vacío
    val isEmpty: Boolean = false
) {
    /**
     * Obtiene los viajes filtrados según los criterios actuales.
     */
    fun getFilteredTrips(): List<Trip> {
        var filtered = trips

        // Filtrar por estado si hay uno seleccionado
        if (selectedStatusFilter != null) {
            filtered = filtered.filter { it.status == selectedStatusFilter }
        }

        // Filtrar por búsqueda si hay query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { trip ->
                trip.cargoName.contains(searchQuery, ignoreCase = true) ||
                        trip.origin.city.contains(searchQuery, ignoreCase = true) ||
                        trip.destination.city.contains(searchQuery, ignoreCase = true) ||
                        trip.cargo.type.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    /**
     * Cuenta viajes por estado.
     */
    fun getTripCountByStatus(status: TripStatus): Int {
        return trips.count { it.status == status }
    }
}