package com.tickettrack.app.ui.trips.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickettrack.app.data.model.trip.toDomain
import com.tickettrack.app.data.repository.trip.TripRepository
import com.tickettrack.app.domain.model.trip.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de viajes.
 *
 * Siguiendo MVVM y Clean Architecture:
 * - Maneja la lógica de negocio de la UI
 * - Se comunica con el repositorio (Data Layer)
 * - Expone estado inmutable a la UI
 * - No contiene referencias a Android Framework (excepto ViewModel y Log)
 */
class TripListViewModel(
    private val tripRepository: TripRepository = TripRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "TripListViewModel"
    }

    // Estado privado mutable
    private val _state = MutableStateFlow(TripListState())

    // Estado público inmutable
    val state: StateFlow<TripListState> = _state.asStateFlow()

    /**
     * Carga los viajes del usuario actual.
     * - Si es admin: carga todos sus viajes
     * - Si es driver: carga solo los asignados a él
     */
    fun loadTrips(userId: String, userRole: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading trips for user: $userId, role: $userRole")

                _state.update { it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentUserId = userId,
                    currentUserRole = userRole
                )}

                val result = if (userRole == "admin" || userRole == "consignatario") {
                    tripRepository.getTripsByAdmin(userId)
                } else {
                    tripRepository.getTripsByDriver(userId)
                }

                result.fold(
                    onSuccess = { tripResponses ->
                        val trips = tripResponses.map { it.toDomain() }

                        _state.update { it.copy(
                            trips = trips,
                            isLoading = false,
                            isEmpty = trips.isEmpty()
                        )}

                        Log.d(TAG, "Loaded ${trips.size} trips successfully")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al cargar viajes"
                        )}

                        Log.e(TAG, "Error loading trips", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error inesperado"
                )}

                Log.e(TAG, "Unexpected error loading trips", e)
            }
        }
    }

    /**
     * Refresca la lista de viajes (pull to refresh).
     */
    fun refreshTrips() {
        viewModelScope.launch {
            try {
                val currentState = _state.value

                Log.d(TAG, "Refreshing trips")

                _state.update { it.copy(isRefreshing = true, errorMessage = null) }

                val result = if (currentState.currentUserRole == "admin" || currentState.currentUserRole == "consignatario") {
                    tripRepository.getTripsByAdmin(currentState.currentUserId)
                } else {
                    tripRepository.getTripsByDriver(currentState.currentUserId)
                }

                result.fold(
                    onSuccess = { tripResponses ->
                        val trips = tripResponses.map { it.toDomain() }

                        _state.update { it.copy(
                            trips = trips,
                            isRefreshing = false,
                            isEmpty = trips.isEmpty()
                        )}

                        Log.d(TAG, "Trips refreshed successfully")
                    },
                    onFailure = { exception ->
                        _state.update { it.copy(
                            isRefreshing = false,
                            errorMessage = exception.message ?: "Error al refrescar"
                        )}

                        Log.e(TAG, "Error refreshing trips", exception)
                    }
                )

            } catch (e: Exception) {
                _state.update { it.copy(
                    isRefreshing = false,
                    errorMessage = e.message ?: "Error inesperado"
                )}

                Log.e(TAG, "Unexpected error refreshing trips", e)
            }
        }
    }

    /**
     * Aplica un filtro de estado.
     */
    fun filterByStatus(status: TripStatus?) {
        Log.d(TAG, "Filtering by status: ${status?.name ?: "ALL"}")

        _state.update { it.copy(selectedStatusFilter = status) }
    }

    /**
     * Actualiza el query de búsqueda.
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    /**
     * Muestra u oculta los filtros.
     */
    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }

    /**
     * Limpia el error actual.
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Limpia todos los filtros.
     */
    fun clearFilters() {
        Log.d(TAG, "Clearing all filters")

        _state.update { it.copy(
            selectedStatusFilter = null,
            searchQuery = ""
        )}
    }

    /**
     * Resetea el estado completo.
     */
    fun resetState() {
        Log.d(TAG, "Resetting state")

        _state.value = TripListState()
    }
}